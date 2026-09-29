package com.academy.lms.enrollment.dto.response;

import com.academy.lms.enrollment.entity.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record CourseStudentResponse(
    UUID enrollmentId, UUID studentId, String studentName, String studentEmail,
    short progress, EnrollmentStatus status, Instant enrolledAt, Instant completedAt,
    Instant lastAccessedAt
) {}
