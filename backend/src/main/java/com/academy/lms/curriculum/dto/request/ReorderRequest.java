package com.academy.lms.curriculum.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ReorderRequest(@NotEmpty @Size(max = 500) List<UUID> ids) {}
