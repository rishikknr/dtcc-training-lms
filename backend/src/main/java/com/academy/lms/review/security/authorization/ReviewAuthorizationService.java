package com.academy.lms.review.security.authorization;

import com.academy.lms.common.exception.ApiException;
import com.academy.lms.review.entity.Review;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReviewAuthorizationService {
  public void requireOwner(Review review, UUID actorId) {
    if (!review.getStudent().getId().equals(actorId)) throw ApiException.forbidden();
  }

  public void requireModerator(Review review, UUID actorId, boolean admin) {
    if (!admin && !review.getCourse().getInstructor().getId().equals(actorId)) {
      throw ApiException.forbidden();
    }
  }
}
