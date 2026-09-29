package com.academy.lms.dashboard.service;

import com.academy.lms.course.entity.CourseStatus;
import com.academy.lms.course.repository.CourseRepository;
import com.academy.lms.dashboard.dto.response.AdminDashboardResponse;
import com.academy.lms.dashboard.dto.response.InstructorDashboardResponse;
import com.academy.lms.dashboard.dto.response.StudentDashboardResponse;
import com.academy.lms.enrollment.repository.EnrollmentRepository;
import com.academy.lms.enrollment.entity.EnrollmentStatus;
import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import com.academy.lms.instructor.repository.InstructorApplicationRepository;
import com.academy.lms.review.entity.ReviewStatus;
import com.academy.lms.review.repository.ReviewRepository;
import com.academy.lms.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
  private final UserRepository users;
  private final CourseRepository courses;
  private final EnrollmentRepository enrollments;
  private final ReviewRepository reviews;
  private final InstructorApplicationRepository applications;

  public DashboardService(UserRepository users, CourseRepository courses,
                          EnrollmentRepository enrollments, ReviewRepository reviews,
                          InstructorApplicationRepository applications) {
    this.users = users;
    this.courses = courses;
    this.enrollments = enrollments;
    this.reviews = reviews;
    this.applications = applications;
  }

  @Transactional(readOnly = true)
  public StudentDashboardResponse student(UUID studentId) {
    var all = enrollments.findByStudentIdAndStatusOrderByLastAccessedAtDesc(
        studentId, EnrollmentStatus.ACTIVE);
    long completed = all.stream().filter(item -> item.getProgress() == 100).count();
    int average = all.isEmpty() ? 0
        : (int) Math.round(all.stream().mapToInt(item -> item.getProgress()).average().orElse(0));
    return new StudentDashboardResponse(all.size(), completed, average);
  }

  @Transactional(readOnly = true)
  public InstructorDashboardResponse instructor(UUID instructorId) {
    return new InstructorDashboardResponse(
        courses.countByInstructorId(instructorId),
        courses.countByInstructorIdAndStatus(instructorId, CourseStatus.PUBLISHED),
        enrollments.countByInstructor(instructorId),
        courses.averageRatingByInstructor(instructorId),
        (int) Math.round(enrollments.averageProgressByInstructor(instructorId)),
        reviews.countByCourseInstructorId(instructorId));
  }

  @Transactional(readOnly = true)
  public AdminDashboardResponse admin() {
    return new AdminDashboardResponse(users.count(), courses.count(), enrollments.count(),
        reviews.count(), applications.countByStatus(InstructorApplicationStatus.PENDING),
        reviews.countByStatus(ReviewStatus.HIDDEN));
  }
}
