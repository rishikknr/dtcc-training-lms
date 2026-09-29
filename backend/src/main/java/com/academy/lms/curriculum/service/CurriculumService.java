package com.academy.lms.curriculum.service;

import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.course.security.authorization.CourseAuthorizationService;
import com.academy.lms.curriculum.dto.request.LessonRequest;
import com.academy.lms.curriculum.dto.request.ReorderRequest;
import com.academy.lms.curriculum.dto.request.SectionRequest;
import com.academy.lms.curriculum.dto.response.LessonResponse;
import com.academy.lms.curriculum.dto.response.SectionResponse;
import com.academy.lms.curriculum.entity.CourseSection;
import com.academy.lms.curriculum.entity.Lesson;
import com.academy.lms.curriculum.mapper.CurriculumMapper;
import com.academy.lms.curriculum.repository.CourseSectionRepository;
import com.academy.lms.curriculum.repository.LessonRepository;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.learning.repository.LessonProgressRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurriculumService {
  private final CourseRepository courses;
  private final CourseSectionRepository sections;
  private final LessonRepository lessons;
  private final CourseAuthorizationService authorization;
  private final CurriculumMapper mapper;
  private final AuditService audit;
  private final EnrollmentRepository enrollments;
  private final LessonProgressRepository progress;

  public CurriculumService(CourseRepository courses, CourseSectionRepository sections,
                           LessonRepository lessons, CourseAuthorizationService authorization,
                           CurriculumMapper mapper, AuditService audit,
                           EnrollmentRepository enrollments, LessonProgressRepository progress) {
    this.courses = courses;
    this.sections = sections;
    this.lessons = lessons;
    this.authorization = authorization;
    this.mapper = mapper;
    this.audit = audit;
    this.enrollments = enrollments;
    this.progress = progress;
  }

  @Transactional
  public SectionResponse addSection(UUID actor, boolean admin, UUID courseId,
                                    SectionRequest request, HttpServletRequest http) {
    Course course = requireCourse(courseId);
    authorization.requireManage(course, actor, admin);
    CourseSection section = sections.save(new CourseSection(course, request.title(),
        Math.toIntExact(sections.countByCourseId(courseId))));
    course.addSection(section);
    audit.record(actor, "SECTION_CREATED", "SECTION", section.getId(), http);
    return mapper.toSection(section, true);
  }

  @Transactional
  public SectionResponse updateSection(UUID actor, boolean admin, UUID sectionId,
                                       SectionRequest request, HttpServletRequest http) {
    CourseSection section = requireSection(sectionId);
    authorization.requireManage(section.getCourse(), actor, admin);
    section.rename(request.title());
    audit.record(actor, "SECTION_UPDATED", "SECTION", sectionId, http);
    return mapper.toSection(section, true);
  }

  @Transactional
  public void deleteSection(UUID actor, boolean admin, UUID sectionId, HttpServletRequest http) {
    CourseSection section = requireSection(sectionId);
    authorization.requireManage(section.getCourse(), actor, admin);
    UUID courseId = section.getCourse().getId();
    sections.delete(section);
    sections.flush();
    compactSections(courseId);
    audit.record(actor, "SECTION_DELETED", "SECTION", sectionId, http);
  }

  @Transactional
  public List<SectionResponse> reorderSections(UUID actor, boolean admin, UUID courseId,
                                               ReorderRequest request, HttpServletRequest http) {
    Course course = requireCourse(courseId);
    authorization.requireManage(course, actor, admin);
    List<CourseSection> ordered = sections.findByCourseIdOrderByPosition(courseId);
    validateExactOrder(request.ids(), ordered.stream().map(CourseSection::getId).toList());
    var byId = ordered.stream().collect(java.util.stream.Collectors.toMap(CourseSection::getId, s -> s));
    for (int i = 0; i < request.ids().size(); i++) byId.get(request.ids().get(i)).reposition(10_000 + i);
    sections.flush();
    for (int i = 0; i < request.ids().size(); i++) byId.get(request.ids().get(i)).reposition(i);
    sections.flush();
    audit.record(actor, "SECTIONS_REORDERED", "COURSE", courseId, http);
    return sections.findByCourseIdOrderByPosition(courseId).stream()
        .map(section -> mapper.toSection(section, true)).toList();
  }

  @Transactional
  public LessonResponse addLesson(UUID actor, boolean admin, UUID sectionId, LessonRequest request,
                                  HttpServletRequest http) {
    CourseSection section = requireSection(sectionId);
    authorization.requireManage(section.getCourse(), actor, admin);
    Lesson lesson = lessons.save(new Lesson(section, request.title(), request.content(),
        request.videoUrl(), Math.toIntExact(lessons.countBySectionId(sectionId)),
        request.durationMinutes(), request.preview()));
    lesson.update(request.title(), request.description(), request.content(), request.videoUrl(),
        request.resourceUrl(), request.durationMinutes(), request.preview(), request.published());
    recalculateCourseProgress(section.getCourse().getId());
    section.addLesson(lesson);
    audit.record(actor, "LESSON_CREATED", "LESSON", lesson.getId(), http);
    return mapper.toLesson(lesson, true);
  }

  @Transactional
  public LessonResponse updateLesson(UUID actor, boolean admin, UUID lessonId, LessonRequest request,
                                     HttpServletRequest http) {
    Lesson lesson = requireLesson(lessonId);
    authorization.requireManage(lesson.getSection().getCourse(), actor, admin);
    lesson.update(request.title(), request.description(), request.content(), request.videoUrl(),
        request.resourceUrl(), request.durationMinutes(), request.preview(), request.published());
    recalculateCourseProgress(lesson.getSection().getCourse().getId());
    audit.record(actor, "LESSON_UPDATED", "LESSON", lessonId, http);
    return mapper.toLesson(lesson, true);
  }

  @Transactional
  public void deleteLesson(UUID actor, boolean admin, UUID lessonId, HttpServletRequest http) {
    Lesson lesson = requireLesson(lessonId);
    authorization.requireManage(lesson.getSection().getCourse(), actor, admin);
    UUID sectionId = lesson.getSection().getId();
    lessons.delete(lesson);
    lessons.flush();
    compactLessons(sectionId);
    recalculateCourseProgress(lesson.getSection().getCourse().getId());
    audit.record(actor, "LESSON_DELETED", "LESSON", lessonId, http);
  }

  @Transactional
  public List<LessonResponse> reorderLessons(UUID actor, boolean admin, UUID sectionId,
                                             ReorderRequest request, HttpServletRequest http) {
    CourseSection section = requireSection(sectionId);
    authorization.requireManage(section.getCourse(), actor, admin);
    List<Lesson> ordered = lessons.findBySectionIdOrderByPosition(sectionId);
    validateExactOrder(request.ids(), ordered.stream().map(Lesson::getId).toList());
    var byId = ordered.stream().collect(java.util.stream.Collectors.toMap(Lesson::getId, l -> l));
    for (int i = 0; i < request.ids().size(); i++) byId.get(request.ids().get(i)).reposition(10_000 + i);
    lessons.flush();
    for (int i = 0; i < request.ids().size(); i++) byId.get(request.ids().get(i)).reposition(i);
    lessons.flush();
    audit.record(actor, "LESSONS_REORDERED", "SECTION", sectionId, http);
    return lessons.findBySectionIdOrderByPosition(sectionId).stream()
        .map(lesson -> mapper.toLesson(lesson, true)).toList();
  }

  private void compactSections(UUID courseId) {
    List<CourseSection> list = sections.findByCourseIdOrderByPosition(courseId);
    for (int i = 0; i < list.size(); i++) list.get(i).reposition(10_000 + i);
    sections.flush();
    for (int i = 0; i < list.size(); i++) list.get(i).reposition(i);
  }

  private void compactLessons(UUID sectionId) {
    List<Lesson> list = lessons.findBySectionIdOrderByPosition(sectionId);
    for (int i = 0; i < list.size(); i++) list.get(i).reposition(10_000 + i);
    lessons.flush();
    for (int i = 0; i < list.size(); i++) list.get(i).reposition(i);
  }

  private void validateExactOrder(List<UUID> requested, List<UUID> actual) {
    if (requested.size() != actual.size() || new HashSet<>(requested).size() != requested.size()
        || !new HashSet<>(requested).equals(new HashSet<>(actual))) {
      throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_ORDER",
          "Reorder request must contain every item exactly once");
    }
  }

  private Course requireCourse(UUID id) {
    return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
  }

  private CourseSection requireSection(UUID id) {
    return sections.findById(id).orElseThrow(() -> ApiException.notFound("Section"));
  }

  private Lesson requireLesson(UUID id) {
    return lessons.findById(id).orElseThrow(() -> ApiException.notFound("Lesson"));
  }

  private void recalculateCourseProgress(UUID courseId) {
    long total = lessons.countBySectionCourseIdAndPublishedTrue(courseId);
    enrollments.findByCourseId(courseId).forEach(enrollment -> {
      long completed = progress.countPublishedByEnrollmentId(enrollment.getId());
      enrollment.updateProgress(total == 0 ? 0 : (int) Math.round(completed * 100.0 / total));
    });
  }
}
