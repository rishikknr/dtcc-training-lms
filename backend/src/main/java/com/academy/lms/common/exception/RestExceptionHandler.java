package com.academy.lms.common.exception;

import com.academy.lms.common.api.ApiError;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> api(ApiException exception) {
    return response(exception.status(), exception.code(), exception.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
    Map<String, String> fields = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(error ->
        fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
    return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fields);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
  ResponseEntity<ApiError> malformed(Exception exception) {
    return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
        "The request contains an invalid or missing value", Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> constraint(DataIntegrityViolationException exception) {
    return response(HttpStatus.CONFLICT, "DATA_CONFLICT",
        "The request conflicts with existing data", Map.of());
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  ResponseEntity<ApiError> optimistic(ObjectOptimisticLockingFailureException exception) {
    return response(HttpStatus.CONFLICT, "STALE_UPDATE",
        "This resource changed while you were editing it. Refresh and try again", Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> denied(AccessDeniedException exception) {
    return response(HttpStatus.FORBIDDEN, "FORBIDDEN",
        "You are not authorized to perform this action", Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception exception) {
    log.error("Unhandled API exception", exception);
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
        "An unexpected error occurred", Map.of());
  }

  private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                            Map<String, String> fields) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status.value(), code, message, fields));
  }
}
