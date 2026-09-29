package com.academy.lms.course.dto.response;

import com.academy.lms.course.entity.CourseLevel;
import com.academy.lms.course.entity.CourseStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record CourseSummaryResponse(
    UUID id,
    String title,
    String slug,
    String shortDescription,
    CourseLevel level,
    CourseStatus status,
    String thumbnailUrl,
    BigDecimal averageRating,
    int ratingCount,
    UUID categoryId,
    String categoryName,
    UUID instructorId,
    String instructorName
) {}
