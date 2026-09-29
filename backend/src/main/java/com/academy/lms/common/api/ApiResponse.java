package com.academy.lms.common.api;

import java.time.Instant;

public record ApiResponse<T>(T data, String message, Instant timestamp) {
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null, Instant.now());
  }

  public static ApiResponse<Void> message(String message) {
    return new ApiResponse<>(null, message, Instant.now());
  }
}
