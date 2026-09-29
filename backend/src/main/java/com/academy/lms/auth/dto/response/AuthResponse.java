package com.academy.lms.auth.dto.response;

import com.academy.lms.user.dto.response.UserProfileResponse;

public record AuthResponse(UserProfileResponse user) {}
