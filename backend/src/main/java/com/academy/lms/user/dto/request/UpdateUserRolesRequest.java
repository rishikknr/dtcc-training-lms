package com.academy.lms.user.dto.request;

import com.academy.lms.user.entity.RoleName;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesRequest(@NotEmpty Set<RoleName> roles) {}
