package com.academy.lms.course.dto.response;

import com.academy.lms.course.entity.CourseLevel;
import com.academy.lms.course.entity.CourseStatus;
import com.academy.lms.curriculum.dto.response.SectionResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseDetailResponse(
    UUID id,
    String title,
    String slug,
    String shortDescription,
    String description,
    CourseLevel level,
    CourseStatus status,
    String thumbnailUrl,
    String prerequisites,
    String learningObjectives,
    String tags,
    String language,
    int durationMinutes,
    BigDecimal averageRating,
    int ratingCount,
    CategoryRef category,
    InstructorRef instructor,
    List<SectionResponse> sections,
    boolean enrolled,
    boolean manageable,
    Instant createdAt,
    Instant updatedAt
) {
  public record CategoryRef(UUID id, String name, String slug) {}
  public record InstructorRef(UUID id, String displayName) {}
}
