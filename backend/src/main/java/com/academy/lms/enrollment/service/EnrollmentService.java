package com.academy.lms.enrollment.service;

import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.entity.Course;
import com.academy.lms.course.entity.CourseStatus;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.enrollment.dto.response.EnrollmentResponse;
import com.academy.lms.enrollment.entity.Enrollment;
import com.academy.lms.enrollment.mapper.EnrollmentMapper;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
  private final EnrollmentRepository enrollments;
  private final CourseRepository courses;
  private final UserRepository users;
  private final EnrollmentMapper mapper;
  private final AuditService audit;

  public EnrollmentService(EnrollmentRepository enrollments, CourseRepository courses,
                           UserRepository users, EnrollmentMapper mapper, AuditService audit) {
    this.enrollments = enrollments;
    this.courses = courses;
    this.users = users;
    this.mapper = mapper;
    this.audit = audit;
  }

  @Transactional
  public EnrollmentResponse enroll(UUID studentId, UUID courseId, HttpServletRequest http) {
    Course course = courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
    if (course.getStatus() != CourseStatus.PUBLISHED) throw ApiException.notFound("Course");
    if (course.getInstructor().getId().equals(studentId)) {
      throw ApiException.conflict("Course instructors cannot enroll in their own course");
    }
    if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) {
      throw ApiException.conflict("Already enrolled in this course");
    }
    User student = users.findById(studentId).orElseThrow(() -> ApiException.notFound("User"));
    Enrollment enrollment = enrollments.save(new Enrollment(student, course));
    audit.record(studentId, "COURSE_ENROLLED", "COURSE", courseId, http);
    return mapper.toResponse(enrollment);
  }

  @Transactional(readOnly = true)
  public List<EnrollmentResponse> mine(UUID studentId) {
    return enrollments.findByStudentIdOrderByLastAccessedAtDesc(studentId).stream()
        .map(mapper::toResponse).toList();
  }
}
