package com.academy.lms.learning.service;

import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.curriculum.entity.Lesson;
import com.academy.lms.curriculum.repository.LessonRepository;
import com.academy.lms.enrollment.entity.Enrollment;
import com.academy.lms.enrollment.entity.EnrollmentStatus;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.learning.dto.request.LessonCompletionRequest;
import com.academy.lms.learning.dto.response.LearningCourseResponse;
import com.academy.lms.learning.dto.response.LessonCompletionResponse;
import com.academy.lms.learning.entity.LessonProgress;
import com.academy.lms.learning.mapper.LearningMapper;
import com.academy.lms.learning.repository.LessonProgressRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningService {
  private final EnrollmentRepository enrollments;
  private final LessonRepository lessons;
  private final LessonProgressRepository progress;
  private final LearningMapper mapper;
  private final AuditService audit;

  public LearningService(EnrollmentRepository enrollments, LessonRepository lessons,
                         LessonProgressRepository progress, LearningMapper mapper,
                         AuditService audit) {
    this.enrollments = enrollments;
    this.lessons = lessons;
    this.progress = progress;
    this.mapper = mapper;
    this.audit = audit;
  }

  @Transactional
  public LearningCourseResponse course(UUID studentId, UUID courseId) {
    Enrollment enrollment = requireEnrollment(studentId, courseId);
    Set<UUID> completed = progress.findByEnrollmentId(enrollment.getId()).stream()
        .map(item -> item.getLesson().getId()).collect(Collectors.toSet());
    enrollment.updateProgress(calculatePercentage(enrollment, completed.size()));
    return mapper.toResponse(enrollment, completed);
  }

  @Transactional
  public LessonCompletionResponse setCompletion(UUID studentId, UUID lessonId,
                                                LessonCompletionRequest request,
                                                HttpServletRequest http) {
    Lesson lesson = lessons.findById(lessonId).orElseThrow(() -> ApiException.notFound("Lesson"));
    if (!lesson.isPublished()) throw ApiException.notFound("Lesson");
    Enrollment enrollment = requireEnrollment(studentId, lesson.getSection().getCourse().getId());
    var existing = progress.findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId);
    if (request.completed() && existing.isEmpty()) {
      progress.save(new LessonProgress(enrollment, lesson));
    } else if (!request.completed() && existing.isPresent()) {
      progress.delete(existing.get());
      progress.flush();
    }
    long completedCount = progress.countPublishedByEnrollmentId(enrollment.getId());
    enrollment.updateProgress(calculatePercentage(enrollment, completedCount));
    audit.record(studentId, request.completed() ? "LESSON_COMPLETED" : "LESSON_REOPENED",
        "LESSON", lessonId, http);
    return new LessonCompletionResponse(lessonId, request.completed(), enrollment.getProgress());
  }

  private int calculatePercentage(Enrollment enrollment, long completedCount) {
    long total = lessons.countBySectionCourseIdAndPublishedTrue(enrollment.getCourse().getId());
    return total == 0 ? 0 : (int) Math.round(completedCount * 100.0 / total);
  }

  private Enrollment requireEnrollment(UUID studentId, UUID courseId) {
    return enrollments.findByStudentIdAndCourseIdAndStatus(studentId, courseId,
            EnrollmentStatus.ACTIVE)
        .orElseThrow(() -> ApiException.forbidden());
  }
}
