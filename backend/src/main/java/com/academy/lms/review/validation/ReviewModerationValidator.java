package com.academy.lms.review.validation;

import com.academy.lms.common.exception.ApiException;
import com.academy.lms.review.dto.request.ModerateReviewRequest;
import com.academy.lms.review.entity.ReviewStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ReviewModerationValidator {
  public void validate(ModerateReviewRequest request) {
    if (request.status() == ReviewStatus.HIDDEN
        && (request.reason() == null || request.reason().isBlank())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "MODERATION_REASON_REQUIRED",
          "A reason is required when hiding a review");
    }
  }
}
