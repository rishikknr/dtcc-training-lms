package com.academy.lms.user.service;

import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import com.academy.lms.user.dto.request.ChangePasswordRequest;
import com.academy.lms.user.dto.request.UpdateProfileRequest;
import com.academy.lms.user.dto.response.UserProfileResponse;
import com.academy.lms.user.entity.User;
import com.academy.lms.user.mapper.UserMapper;
import com.academy.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository users;
  private final UserMapper mapper;
  private final PasswordEncoder passwords;
  private final AuditService audit;

  public UserService(UserRepository users, UserMapper mapper, PasswordEncoder passwords,
                     AuditService audit) {
    this.users = users;
    this.mapper = mapper;
    this.passwords = passwords;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public UserProfileResponse getProfile(UUID userId) {
    return mapper.toProfile(requireUser(userId));
  }

  @Transactional
  public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request,
                                           HttpServletRequest http) {
    User user = requireUser(userId);
    user.updateProfile(request.displayName(), request.bio(), request.avatarUrl());
    audit.record(userId, "PROFILE_UPDATED", "USER", userId, http);
    return mapper.toProfile(user);
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest request, HttpServletRequest http) {
    User user = requireUser(userId);
    if (!passwords.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD",
          "Current password is incorrect");
    }
    if (passwords.matches(request.newPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_REUSE",
          "New password must be different from the current password");
    }
    user.changePassword(passwords.encode(request.newPassword()));
    audit.record(userId, "PASSWORD_CHANGED", "USER", userId, http);
  }

  private User requireUser(UUID id) {
    return users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
  }
}
