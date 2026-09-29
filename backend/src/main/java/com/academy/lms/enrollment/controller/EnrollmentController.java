package com.academy.lms.enrollment.controller;

import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.enrollment.dto.response.EnrollmentResponse;
import com.academy.lms.enrollment.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrollmentController {
  private final EnrollmentService service;

  public EnrollmentController(EnrollmentService service) { this.service = service; }

  @PostMapping("/api/courses/{id}/enroll")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('STUDENT')")
  public EnrollmentResponse enroll(@PathVariable UUID id, Authentication authentication,
                                   HttpServletRequest http) {
    return service.enroll(CurrentUser.id(authentication), id, http);
  }

  @GetMapping("/api/enrollments")
  @PreAuthorize("hasRole('STUDENT')")
  public List<EnrollmentResponse> mine(Authentication authentication) {
    return service.mine(CurrentUser.id(authentication));
  }
}
