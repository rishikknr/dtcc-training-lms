package com.academy.lms.instructor.mapper;

import com.academy.lms.instructor.dto.response.InstructorApplicationResponse;
import com.academy.lms.instructor.entity.InstructorApplication;
import org.springframework.stereotype.Component;

@Component
public class InstructorApplicationMapper {
  public InstructorApplicationResponse toResponse(InstructorApplication application) {
    return new InstructorApplicationResponse(
        application.getId(), application.getApplicant().getId(),
        application.getApplicant().getDisplayName(), application.getApplicant().getEmail(),
        application.getExpertise(), application.getMotivation(), application.getPortfolioUrl(),
        application.getStatus(), application.getReviewNote(), application.getCreatedAt(),
        application.getReviewedAt());
  }
}
