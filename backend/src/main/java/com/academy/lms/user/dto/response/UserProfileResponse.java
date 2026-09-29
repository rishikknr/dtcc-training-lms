package com.academy.lms.user.dto.response;

import com.academy.lms.user.entity.RoleName;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String email,
    String displayName,
    String bio,
    String avatarUrl,
    Set<RoleName> roles
) {}
