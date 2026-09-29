package com.academy.lms.curriculum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionRequest(@NotBlank @Size(max = 160) String title) {}
