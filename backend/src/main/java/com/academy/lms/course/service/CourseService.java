package com.academy.lms.course.service;

import com.academy.lms.category.entity.Category;
import com.academy.lms.category.service.CategoryService;
import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.common.security.AuthenticatedUser;
import com.academy.lms.course.dto.request.AssignInstructorRequest;
import com.academy.lms.course.dto.request.ChangeCourseStatusRequest;
import com.academy.lms.course.dto.request.CourseUpsertRequest;
import com.academy.lms.course.dto.response.CourseDetailResponse;
import com.academy.lms.course.dto.response.CourseSummaryResponse;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.entity.CourseLevel;
import com.academy.lms.course.entity.CourseStatus;
import com.academy.lms.course.mapper.CourseMapper;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.course.security.authorization.CourseAuthorizationService;
import com.academy.lms.course.validation.CoursePublishingValidator;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.user.entity.RoleName;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {
  private final CourseRepository courses;
  private final CategoryService categories;
  private final UserRepository users;
  private final EnrollmentRepository enrollments;
  private final CourseMapper mapper;
  private final SlugService slugs;
  private final CourseAuthorizationService authorization;
  private final CoursePublishingValidator publishing;
  private final AuditService audit;

  public CourseService(CourseRepository courses, CategoryService categories, UserRepository users,
                       EnrollmentRepository enrollments, CourseMapper mapper, SlugService slugs,
                       CourseAuthorizationService authorization,
                       CoursePublishingValidator publishing, AuditService audit) {
    this.courses = courses;
    this.categories = categories;
    this.users = users;
    this.enrollments = enrollments;
    this.mapper = mapper;
    this.slugs = slugs;
    this.authorization = authorization;
    this.publishing = publishing;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public PageResponse<CourseSummaryResponse> catalog(String query, UUID categoryId,
                                                     CourseLevel level, int page, int size,
                                                     String sort) {
    int safeSize = Math.min(Math.max(size, 1), 50);
    Map<String, String> sortFields = Map.of(
        "newest", "publishedAt", "title", "title", "rating", "averageRating");
    String property = sortFields.getOrDefault(sort, "publishedAt");
    Sort.Direction direction = "title".equals(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Specification<Course> specification = (root, criteria, builder) -> {
      var predicates = new ArrayList<Predicate>();
      predicates.add(builder.equal(root.get("status"), CourseStatus.PUBLISHED));
      if (query != null && !query.isBlank()) {
        String term = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
        predicates.add(builder.or(
            builder.like(builder.lower(root.get("title")), term),
            builder.like(builder.lower(root.get("shortDescription")), term),
            builder.like(builder.lower(root.get("description")), term)));
      }
      if (categoryId != null) predicates.add(builder.equal(root.get("category").get("id"), categoryId));
      if (level != null) predicates.add(builder.equal(root.get("level"), level));
      return builder.and(predicates.toArray(Predicate[]::new));
    };
    return PageResponse.from(courses.findAll(specification,
        PageRequest.of(Math.max(page, 0), safeSize, Sort.by(direction, property)))
        .map(mapper::toSummary));
  }

  @Transactional(readOnly = true)
  public CourseDetailResponse get(UUID id, Authentication authentication) {
    Course course = requireCourse(id);
    UUID actorId = principalId(authentication);
    boolean admin = hasRole(authentication, "ADMIN");
    boolean manageable = authorization.canManage(course, actorId, admin);
    if (course.getStatus() != CourseStatus.PUBLISHED && !manageable) throw ApiException.notFound("Course");
    boolean enrolled = actorId != null && enrollments.existsByStudentIdAndCourseId(actorId, id);
    return mapper.toDetail(course, manageable || enrolled, enrolled, manageable);
  }

  @Transactional
  public CourseDetailResponse create(UUID actorId, CourseUpsertRequest request,
                                     HttpServletRequest http) {
    User actor = requireUser(actorId);
    Category category = categories.require(request.categoryId());
    Course course = courses.save(new Course(actor, category, request.title(),
        slugs.unique(request.title()), request.shortDescription(), request.description(),
        request.level(), request.thumbnailUrl()));
    audit.record(actorId, "COURSE_CREATED", "COURSE", course.getId(), http);
    return mapper.toDetail(course, true, false, true);
  }

  @Transactional
  public CourseDetailResponse update(UUID actorId, boolean admin, UUID id,
                                     CourseUpsertRequest request, HttpServletRequest http) {
    Course course = requireManaged(actorId, admin, id);
    String oldTitle = course.getTitle();
    course.update(categories.require(request.categoryId()), request.title(),
        request.shortDescription(), request.description(), request.level(), request.thumbnailUrl());
    if (!oldTitle.equalsIgnoreCase(request.title().trim())) {
      course.changeSlug(slugs.unique(request.title()));
    }
    audit.record(actorId, "COURSE_UPDATED", "COURSE", id, http);
    return mapper.toDetail(course, true,
        enrollments.existsByStudentIdAndCourseId(actorId, id), true);
  }

  @Transactional
  public CourseDetailResponse changeStatus(UUID actorId, boolean admin, UUID id,
                                           ChangeCourseStatusRequest request,
                                           HttpServletRequest http) {
    Course course = requireManaged(actorId, admin, id);
    CourseStatus target = request.status();
    if (target == CourseStatus.PUBLISHED) publishing.validate(course);
    if (target == CourseStatus.DRAFT && course.getStatus() == CourseStatus.PUBLISHED
        && enrollments.countByCourseId(id) > 0) {
      throw ApiException.conflict("A course with enrolled learners cannot be returned to draft; archive it instead");
    }
    course.changeStatus(target);
    audit.record(actorId, "COURSE_STATUS_CHANGED", "COURSE", id,
        Map.of("status", target.name()), http);
    return mapper.toDetail(course, true,
        enrollments.existsByStudentIdAndCourseId(actorId, id), true);
  }

  @Transactional
  public void delete(UUID actorId, boolean admin, UUID id, HttpServletRequest http) {
    Course course = requireManaged(actorId, admin, id);
    if (course.getStatus() != CourseStatus.DRAFT || enrollments.countByCourseId(id) > 0) {
      throw ApiException.conflict("Only draft courses without enrollments can be deleted; archive this course instead");
    }
    courses.delete(course);
    audit.record(actorId, "COURSE_DELETED", "COURSE", id, http);
  }

  @Transactional(readOnly = true)
  public PageResponse<CourseSummaryResponse> managed(UUID actorId, boolean admin, int page, int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.DESC, "updatedAt"));
    var result = admin ? courses.findAll(pageable) : courses.findByInstructorId(actorId, pageable);
    return PageResponse.from(result.map(mapper::toSummary));
  }

  @Transactional
  public CourseDetailResponse assignInstructor(UUID actorId, UUID courseId,
                                               AssignInstructorRequest request,
                                               HttpServletRequest http) {
    Course course = requireCourse(courseId);
    User instructor = requireUser(request.instructorId());
    if (!instructor.hasRole(RoleName.INSTRUCTOR)) {
      throw ApiException.conflict("Selected user is not an approved instructor");
    }
    course.assignInstructor(instructor);
    audit.record(actorId, "COURSE_INSTRUCTOR_ASSIGNED", "COURSE", courseId,
        Map.of("instructorId", instructor.getId().toString()), http);
    return mapper.toDetail(course, true, false, true);
  }

  public Course requireCourse(UUID id) {
    return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
  }

  private Course requireManaged(UUID actorId, boolean admin, UUID id) {
    Course course = requireCourse(id);
    authorization.requireManage(course, actorId, admin);
    return course;
  }

  private User requireUser(UUID id) {
    return users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
  }

  private UUID principalId(Authentication authentication) {
    return authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
        ? user.id() : null;
  }

  private boolean hasRole(Authentication authentication, String role) {
    return authentication != null && authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
  }
}
