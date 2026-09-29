package com.academy.lms.enrollment.mapper;

import com.academy.lms.course.mapper.CourseMapper;
import com.academy.lms.enrollment.dto.response.EnrollmentResponse;
import com.academy.lms.enrollment.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
  private final CourseMapper courses;

  public EnrollmentMapper(CourseMapper courses) { this.courses = courses; }

  public EnrollmentResponse toResponse(Enrollment enrollment) {
    return new EnrollmentResponse(enrollment.getId(), courses.toSummary(enrollment.getCourse()),
        enrollment.getProgress(), enrollment.getEnrolledAt(), enrollment.getCompletedAt(),
        enrollment.getLastAccessedAt());
  }
}
