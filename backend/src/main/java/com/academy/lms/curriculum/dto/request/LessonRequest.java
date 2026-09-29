package com.academy.lms.curriculum.dto.request;

import com.academy.lms.common.validation.ValidHttpUrl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(
    @NotBlank @Size(max = 160) String title,
    @Size(max = 500) String description,
    @NotBlank @Size(max = 50_000) String content,
    @Size(max = 500) @ValidHttpUrl String videoUrl,
    @Size(max = 500) @ValidHttpUrl String resourceUrl,
    @Min(0) @Max(1440) int durationMinutes,
    boolean preview,
    boolean published
) {}
