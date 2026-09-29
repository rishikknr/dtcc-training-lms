package com.academy.lms.review.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record ReviewSummaryResponse(BigDecimal averageRating, long totalReviews,
                                    Map<Integer, Long> distribution) {}
