package com.academy.lms.review.controller;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.review.dto.request.CreateReviewRequest;
import com.academy.lms.review.dto.request.ModerateReviewRequest;
import com.academy.lms.review.dto.request.UpdateReviewRequest;
import com.academy.lms.review.dto.response.ReviewResponse;
import com.academy.lms.review.dto.response.ReviewSummaryResponse;
import com.academy.lms.review.entity.ReviewStatus;
import com.academy.lms.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {
  private final ReviewService service;

  public ReviewController(ReviewService service) { this.service = service; }

  @PostMapping("/api/reviews")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('STUDENT')")
  public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request,
                               Authentication authentication, HttpServletRequest http) {
    return service.create(CurrentUser.id(authentication), request, http);
  }

  @GetMapping("/api/courses/{id}/reviews")
  public PageResponse<ReviewResponse> list(@PathVariable UUID id,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(defaultValue = "newest") String sort) {
    return service.listPublished(id, page, size, sort);
  }

  @GetMapping("/api/courses/{id}/reviews/summary")
  public ReviewSummaryResponse summary(@PathVariable UUID id) { return service.summary(id); }

  @GetMapping("/api/reviews/mine")
  @PreAuthorize("hasRole('STUDENT')")
  public PageResponse<ReviewResponse> mine(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Authentication authentication) {
    return service.mine(CurrentUser.id(authentication), page, size);
  }

  @GetMapping("/api/reviews/moderation")
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public PageResponse<ReviewResponse> moderationQueue(
      @RequestParam(required = false) UUID courseId,
      @RequestParam(required = false) ReviewStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Authentication authentication) {
    return service.moderationQueue(CurrentUser.id(authentication), isAdmin(authentication), courseId,
        status, page, size);
  }

  @PutMapping("/api/reviews/{id}")
  @PreAuthorize("hasRole('STUDENT')")
  public ReviewResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateReviewRequest request,
                               Authentication authentication, HttpServletRequest http) {
    return service.update(CurrentUser.id(authentication), id, request, http);
  }

  @DeleteMapping("/api/reviews/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, Authentication authentication,
                     HttpServletRequest http) {
    service.delete(CurrentUser.id(authentication), isAdmin(authentication), id, http);
  }

  @PostMapping("/api/reviews/{id}/moderate")
  @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
  public ReviewResponse moderate(@PathVariable UUID id,
                                 @Valid @RequestBody ModerateReviewRequest request,
                                 Authentication authentication, HttpServletRequest http) {
    return service.moderate(CurrentUser.id(authentication), isAdmin(authentication), id, request,
        http);
  }

  private boolean isAdmin(Authentication authentication) {
    return CurrentUser.hasRole(authentication, "ADMIN");
  }
}
