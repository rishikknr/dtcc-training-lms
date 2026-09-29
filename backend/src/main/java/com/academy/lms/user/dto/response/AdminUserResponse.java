package com.academy.lms.user.dto.response;

import com.academy.lms.user.entity.RoleName;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
    UUID id,
    String email,
    String displayName,
    boolean enabled,
    Set<RoleName> roles,
    Instant createdAt
) {}
