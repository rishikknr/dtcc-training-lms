package com.academy.lms.auth.controller;

import com.academy.lms.auth.dto.request.LoginRequest;
import com.academy.lms.auth.dto.request.RegisterRequest;
import com.academy.lms.auth.dto.response.AuthResponse;
import com.academy.lms.auth.service.AuthService;
import com.academy.lms.common.security.CurrentUser;
import com.academy.lms.user.dto.response.UserProfileResponse;
import com.academy.lms.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;
  private final UserService users;

  public AuthController(AuthService auth, UserService users) {
    this.auth = auth;
    this.users = users;
  }

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest request,
                               HttpServletRequest http) {
    return auth.register(request, http);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
    return auth.login(request, http);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(Authentication authentication, HttpServletRequest http) {
    auth.logout(CurrentUser.id(authentication), http);
  }

  @GetMapping("/me")
  public UserProfileResponse me(Authentication authentication) {
    return users.getProfile(CurrentUser.id(authentication));
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(Authentication authentication, HttpServletRequest http) {
    return auth.refresh(CurrentUser.id(authentication), http);
  }
}
