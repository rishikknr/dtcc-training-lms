package com.academy.lms.course.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignInstructorRequest(@NotNull UUID instructorId) {}
