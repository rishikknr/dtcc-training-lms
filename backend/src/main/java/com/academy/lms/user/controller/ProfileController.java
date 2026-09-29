package com.academy.lms.user.controller;

import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.user.dto.request.ChangePasswordRequest;
import com.academy.lms.user.dto.request.UpdateProfileRequest;
import com.academy.lms.user.dto.response.UserProfileResponse;
import com.academy.lms.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
  private final UserService service;

  public ProfileController(UserService service) { this.service = service; }

  @GetMapping
  public UserProfileResponse get(Authentication authentication) {
    return service.getProfile(CurrentUser.id(authentication));
  }

  @PutMapping
  public UserProfileResponse update(@Valid @RequestBody UpdateProfileRequest request,
                                    Authentication authentication, HttpServletRequest http) {
    return service.updateProfile(CurrentUser.id(authentication), request, http);
  }

  @PostMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                             Authentication authentication, HttpServletRequest http) {
    service.changePassword(CurrentUser.id(authentication), request, http);
  }
}
