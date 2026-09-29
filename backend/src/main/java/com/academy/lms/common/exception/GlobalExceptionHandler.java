package com.academy.lms.common.exception;
import com.academy.lms.common.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class) ResponseEntity<ApiError> api(ApiException e){return ResponseEntity.status(e.status()).body(err(e.status(),e.code(),e.getMessage(),Map.of()));}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException e){
    Map<String,String> fields=new LinkedHashMap<>(); e.getBindingResult().getFieldErrors().forEach(x->fields.putIfAbsent(x.getField(),x.getDefaultMessage()));
    return ResponseEntity.badRequest().body(err(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Request validation failed",fields));
  }
  @ExceptionHandler({DataIntegrityViolationException.class}) ResponseEntity<ApiError> conflict(Exception e){return ResponseEntity.status(409).body(err(HttpStatus.CONFLICT,"CONFLICT","The request conflicts with existing data",Map.of()));}
  @ExceptionHandler(AccessDeniedException.class) ResponseEntity<ApiError> denied(){return ResponseEntity.status(403).body(err(HttpStatus.FORBIDDEN,"FORBIDDEN","You are not authorized to perform this action",Map.of()));}
  @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception e,HttpServletRequest req){return ResponseEntity.status(500).body(err(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","An unexpected error occurred",Map.of()));}
  private ApiError err(HttpStatus s,String c,String m,Map<String,String> f){return new ApiError(Instant.now(),s.value(),c,m,f);}
}
