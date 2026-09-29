package com.academy.lms.instructor.dto.request;

import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InstructorDecisionRequest(
    @NotNull InstructorApplicationStatus status,
    @Size(max = 1000) String note
) {}
