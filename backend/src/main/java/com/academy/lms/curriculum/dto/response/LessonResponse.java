package com.academy.lms.curriculum.dto.response;

import java.util.UUID;

public record LessonResponse(
    UUID id,
    String title,
    String description,
    String content,
    String videoUrl,
    String resourceUrl,
    int position,
    int durationMinutes,
    boolean preview,
    boolean published
) {}
