package com.academy.lms.curriculum.controller;

import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.curriculum.dto.request.LessonRequest;
import com.academy.lms.curriculum.dto.request.ReorderRequest;
import com.academy.lms.curriculum.dto.request.SectionRequest;
import com.academy.lms.curriculum.dto.response.LessonResponse;
import com.academy.lms.curriculum.dto.response.SectionResponse;
import com.academy.lms.curriculum.service.CurriculumService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
public class CurriculumController {
  private final CurriculumService service;

  public CurriculumController(CurriculumService service) { this.service = service; }

  @PostMapping("/courses/{courseId}/sections")
  @ResponseStatus(HttpStatus.CREATED)
  public SectionResponse addSection(@PathVariable UUID courseId,
                                    @Valid @RequestBody SectionRequest request,
                                    Authentication authentication, HttpServletRequest http) {
    return service.addSection(CurrentUser.id(authentication), isAdmin(authentication), courseId,
        request, http);
  }

  @PutMapping("/sections/{sectionId}")
  public SectionResponse updateSection(@PathVariable UUID sectionId,
                                       @Valid @RequestBody SectionRequest request,
                                       Authentication authentication, HttpServletRequest http) {
    return service.updateSection(CurrentUser.id(authentication), isAdmin(authentication), sectionId,
        request, http);
  }

  @DeleteMapping("/sections/{sectionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSection(@PathVariable UUID sectionId, Authentication authentication,
                            HttpServletRequest http) {
    service.deleteSection(CurrentUser.id(authentication), isAdmin(authentication), sectionId, http);
  }

  @PutMapping("/courses/{courseId}/sections/order")
  public List<SectionResponse> reorderSections(@PathVariable UUID courseId,
                                               @Valid @RequestBody ReorderRequest request,
                                               Authentication authentication,
                                               HttpServletRequest http) {
    return service.reorderSections(CurrentUser.id(authentication), isAdmin(authentication), courseId,
        request, http);
  }

  @PostMapping("/sections/{sectionId}/lessons")
  @ResponseStatus(HttpStatus.CREATED)
  public LessonResponse addLesson(@PathVariable UUID sectionId,
                                  @Valid @RequestBody LessonRequest request,
                                  Authentication authentication, HttpServletRequest http) {
    return service.addLesson(CurrentUser.id(authentication), isAdmin(authentication), sectionId,
        request, http);
  }

  @PutMapping("/lessons/{lessonId}")
  public LessonResponse updateLesson(@PathVariable UUID lessonId,
                                     @Valid @RequestBody LessonRequest request,
                                     Authentication authentication, HttpServletRequest http) {
    return service.updateLesson(CurrentUser.id(authentication), isAdmin(authentication), lessonId,
        request, http);
  }

  @DeleteMapping("/lessons/{lessonId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLesson(@PathVariable UUID lessonId, Authentication authentication,
                           HttpServletRequest http) {
    service.deleteLesson(CurrentUser.id(authentication), isAdmin(authentication), lessonId, http);
  }

  @PutMapping("/sections/{sectionId}/lessons/order")
  public List<LessonResponse> reorderLessons(@PathVariable UUID sectionId,
                                             @Valid @RequestBody ReorderRequest request,
                                             Authentication authentication,
                                             HttpServletRequest http) {
    return service.reorderLessons(CurrentUser.id(authentication), isAdmin(authentication), sectionId,
        request, http);
  }

  private boolean isAdmin(Authentication authentication) {
    return CurrentUser.hasRole(authentication, "ADMIN");
  }
}
