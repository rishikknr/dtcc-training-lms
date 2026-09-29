package com.academy.lms.user.controller;

import com.academy.lms.common.api.PageResponse;
import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.user.dto.request.UpdateUserStatusRequest;
import com.academy.lms.user.dto.response.AdminUserResponse;
import com.academy.lms.user.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  private final AdminUserService service;

  public AdminUserController(AdminUserService service) { this.service = service; }

  @GetMapping
  public PageResponse<AdminUserResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.list(q, page, size);
  }

  @PatchMapping("/{id}/status")
  public AdminUserResponse updateStatus(@PathVariable UUID id,
                                        @Valid @RequestBody UpdateUserStatusRequest request,
                                        Authentication authentication, HttpServletRequest http) {
    return service.updateStatus(CurrentUser.id(authentication), id, request, http);
  }
}
