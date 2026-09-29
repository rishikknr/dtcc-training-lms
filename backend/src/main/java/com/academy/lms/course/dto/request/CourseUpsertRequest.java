package com.academy.lms.course.dto.request;

import com.academy.lms.common.validation.ValidHttpUrl;
import com.academy.lms.course.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CourseUpsertRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 300) String shortDescription,
    @NotBlank @Size(max = 20_000) String description,
    @NotNull CourseLevel level,
    UUID categoryId,
    @Size(max = 500) @ValidHttpUrl String thumbnailUrl
) {}
