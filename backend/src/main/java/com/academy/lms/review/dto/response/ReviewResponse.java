package com.academy.lms.review.dto.response;

import com.academy.lms.review.entity.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID courseId,
    String courseTitle,
    UUID studentId,
    String studentName,
    short rating,
    String comment,
    ReviewStatus status,
    String moderationReason,
    Instant createdAt,
    Instant updatedAt
) {}
