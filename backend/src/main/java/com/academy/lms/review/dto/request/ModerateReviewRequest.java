package com.academy.lms.review.dto.request;

import com.academy.lms.review.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerateReviewRequest(
    @NotNull ReviewStatus status,
    @Size(max = 500) String reason
) {}
