package com.academy.lms.user.mapper;

import com.academy.lms.user.dto.response.AdminUserResponse;
import com.academy.lms.user.dto.response.UserProfileResponse;
import com.academy.lms.user.entity.Role;
import com.academy.lms.user.entity.User;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserProfileResponse toProfile(User user) {
    return new UserProfileResponse(
        user.getId(), user.getEmail(), user.getDisplayName(), user.getBio(), user.getAvatarUrl(),
        user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet()));
  }

  public AdminUserResponse toAdmin(User user) {
    return new AdminUserResponse(
        user.getId(), user.getEmail(), user.getDisplayName(), user.isEnabled(),
        user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet()),
        user.getCreatedAt());
  }
}
