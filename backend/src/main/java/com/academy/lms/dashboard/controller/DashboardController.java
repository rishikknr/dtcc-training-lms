package com.academy.lms.dashboard.controller;

import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.dashboard.dto.response.AdminDashboardResponse;
import com.academy.lms.dashboard.dto.response.InstructorDashboardResponse;
import com.academy.lms.dashboard.dto.response.StudentDashboardResponse;
import com.academy.lms.dashboard.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final DashboardService service;

  public DashboardController(DashboardService service) { this.service = service; }

  @GetMapping("/student")
  @PreAuthorize("hasRole('STUDENT')")
  public StudentDashboardResponse student(Authentication authentication) {
    return service.student(CurrentUser.id(authentication));
  }

  @GetMapping("/instructor")
  @PreAuthorize("hasRole('INSTRUCTOR')")
  public InstructorDashboardResponse instructor(Authentication authentication) {
    return service.instructor(CurrentUser.id(authentication));
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public AdminDashboardResponse admin() { return service.admin(); }
}
