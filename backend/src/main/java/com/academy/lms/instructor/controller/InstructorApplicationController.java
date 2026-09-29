package com.academy.lms.instructor.controller;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.instructor.dto.request.InstructorApplicationRequest;
import com.academy.lms.instructor.dto.request.InstructorDecisionRequest;
import com.academy.lms.instructor.dto.response.InstructorApplicationResponse;
import com.academy.lms.instructor.entity.InstructorApplicationStatus;
import com.academy.lms.instructor.service.InstructorApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor-applications")
public class InstructorApplicationController {
  private final InstructorApplicationService service;

  public InstructorApplicationController(InstructorApplicationService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('STUDENT')")
  public InstructorApplicationResponse submit(
      @Valid @RequestBody InstructorApplicationRequest request,
      Authentication authentication, HttpServletRequest http) {
    return service.submit(CurrentUser.id(authentication), request, http);
  }

  @GetMapping("/mine")
  @PreAuthorize("hasRole('STUDENT')")
  public InstructorApplicationResponse mine(Authentication authentication) {
    return service.mine(CurrentUser.id(authentication));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public PageResponse<InstructorApplicationResponse> list(
      @RequestParam(required = false) InstructorApplicationStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.list(status, page, size);
  }

  @PatchMapping("/{id}/decision")
  @PreAuthorize("hasRole('ADMIN')")
  public InstructorApplicationResponse decide(
      @PathVariable UUID id, @Valid @RequestBody InstructorDecisionRequest request,
      Authentication authentication, HttpServletRequest http) {
    return service.decide(CurrentUser.id(authentication), id, request, http);
  }
}
