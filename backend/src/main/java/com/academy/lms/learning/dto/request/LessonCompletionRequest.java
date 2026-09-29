package com.academy.lms.learning.dto.request;

import jakarta.validation.constraints.NotNull;

public record LessonCompletionRequest(@NotNull Boolean completed) {}
