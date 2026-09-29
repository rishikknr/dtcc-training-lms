package com.academy.lms.enrollment.dto.response;

import com.academy.lms.course.dto.response.CourseSummaryResponse;
import com.academy.lms.enrollment.entity.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
    UUID id,
    CourseSummaryResponse course,
    short progress,
    Instant enrolledAt,
    Instant completedAt,
    Instant lastAccessedAt,
    EnrollmentStatus status,
    Instant cancelledAt
) {}
