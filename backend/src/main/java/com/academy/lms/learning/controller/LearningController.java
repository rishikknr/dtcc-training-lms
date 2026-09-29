package com.academy.lms.learning.controller;

import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.learning.dto.request.LessonCompletionRequest;
import com.academy.lms.learning.dto.response.LearningCourseResponse;
import com.academy.lms.learning.dto.response.LessonCompletionResponse;
import com.academy.lms.learning.service.LearningService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning")
@PreAuthorize("hasRole('STUDENT')")
public class LearningController {
  private final LearningService service;

  public LearningController(LearningService service) { this.service = service; }

  @GetMapping("/courses/{courseId}")
  public LearningCourseResponse course(@PathVariable UUID courseId,
                                       Authentication authentication) {
    return service.course(CurrentUser.id(authentication), courseId);
  }

  @PutMapping("/lessons/{lessonId}/completion")
  public LessonCompletionResponse setCompletion(
      @PathVariable UUID lessonId, @Valid @RequestBody LessonCompletionRequest request,
      Authentication authentication, HttpServletRequest http) {
    return service.setCompletion(CurrentUser.id(authentication), lessonId, request, http);
  }
}
