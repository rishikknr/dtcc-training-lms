package com.academy.lms.instructor.dto.response;

import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record InstructorApplicationResponse(
    UUID id,
    UUID applicantId,
    String applicantName,
    String applicantEmail,
    String expertise,
    String motivation,
    String portfolioUrl,
    InstructorApplicationStatus status,
    String reviewNote,
    Instant createdAt,
    Instant reviewedAt
) {}
