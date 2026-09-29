package com.academy.lms.category.dto.response;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String slug, String description) {}
