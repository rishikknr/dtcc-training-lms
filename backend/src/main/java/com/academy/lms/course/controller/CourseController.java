package com.academy.lms.course.controller;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.course.dto.request.AssignInstructorRequest;
import com.academy.lms.course.dto.request.ChangeCourseStatusRequest;
import com.academy.lms.course.dto.request.CourseUpsertRequest;
import com.academy.lms.course.dto.response.CourseDetailResponse;
import com.academy.lms.course.dto.response.CourseSummaryResponse;
import com.academy.lms.course.entity.CourseLevel;
import com.academy.lms.course.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
  private final CourseService service;

  public CourseController(CourseService service) { this.service = service; }

  @GetMapping
  public PageResponse<CourseSummaryResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID category,
      @RequestParam(required = false) CourseLevel level,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "newest") String sort) {
    return service.catalog(q, category, level, page, size, sort);
  }

  @GetMapping("/managed")
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public PageResponse<CourseSummaryResponse> managed(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Authentication authentication) {
    return service.managed(CurrentUser.id(authentication), isAdmin(authentication), page, size);
  }

  @GetMapping("/{id}")
  public CourseDetailResponse get(@PathVariable UUID id, Authentication authentication) {
    return service.get(id, authentication);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public CourseDetailResponse create(@Valid @RequestBody CourseUpsertRequest request,
                                     Authentication authentication, HttpServletRequest http) {
    return service.create(CurrentUser.id(authentication), request, http);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public CourseDetailResponse update(@PathVariable UUID id,
                                     @Valid @RequestBody CourseUpsertRequest request,
                                     Authentication authentication, HttpServletRequest http) {
    return service.update(CurrentUser.id(authentication), isAdmin(authentication), id, request, http);
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public CourseDetailResponse changeStatus(@PathVariable UUID id,
                                           @Valid @RequestBody ChangeCourseStatusRequest request,
                                           Authentication authentication, HttpServletRequest http) {
    return service.changeStatus(CurrentUser.id(authentication), isAdmin(authentication), id, request,
        http);
  }

  @PostMapping("/{id}/publish")
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public CourseDetailResponse publish(@PathVariable UUID id, Authentication authentication,
                                      HttpServletRequest http) {
    return service.changeStatus(CurrentUser.id(authentication), isAdmin(authentication), id,
        new ChangeCourseStatusRequest(com.academy.lms.course.entity.CourseStatus.PUBLISHED), http);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public void delete(@PathVariable UUID id, Authentication authentication,
                     HttpServletRequest http) {
    service.delete(CurrentUser.id(authentication), isAdmin(authentication), id, http);
  }

  @PutMapping("/{id}/instructor")
  @PreAuthorize("hasRole('ADMIN')")
  public CourseDetailResponse assignInstructor(@PathVariable UUID id,
                                               @Valid @RequestBody AssignInstructorRequest request,
                                               Authentication authentication,
                                               HttpServletRequest http) {
    return service.assignInstructor(CurrentUser.id(authentication), id, request, http);
  }

  private boolean isAdmin(Authentication authentication) {
    return CurrentUser.hasRole(authentication, "ADMIN");
  }
}
