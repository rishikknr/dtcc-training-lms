package com.academy.lms.review.mapper;

import com.academy.lms.review.dto.response.ReviewResponse;
import com.academy.lms.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
  public ReviewResponse toResponse(Review review) {
    return new ReviewResponse(review.getId(), review.getCourse().getId(),
        review.getCourse().getTitle(), review.getStudent().getId(),
        review.getStudent().getDisplayName(), review.getRating(), review.getComment(),
        review.getStatus(), review.getModerationReason(), review.getCreatedAt(),
        review.getUpdatedAt());
  }
}
