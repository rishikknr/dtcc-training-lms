package com.academy.lms.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
    @Min(1) @Max(5) short rating,
    @NotBlank @Size(max = 2000) String comment
) {}
