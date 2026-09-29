package com.academy.lms.category.controller;

import com.academy.lms.category.dto.request.CategoryRequest;
import com.academy.lms.category.dto.response.CategoryResponse;
import com.academy.lms.category.service.CategoryService;
import com.academy.lms.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  private final CategoryService service;

  public CategoryController(CategoryService service) { this.service = service; }

  @GetMapping public List<CategoryResponse> list() { return service.list(); }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CategoryResponse create(@Valid @RequestBody CategoryRequest request,
                                 Authentication authentication, HttpServletRequest http) {
    return service.create(CurrentUser.id(authentication), request, http);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request,
                                 Authentication authentication, HttpServletRequest http) {
    return service.update(CurrentUser.id(authentication), id, request, http);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID id, Authentication authentication,
                     HttpServletRequest http) {
    service.delete(CurrentUser.id(authentication), id, http);
  }
}
