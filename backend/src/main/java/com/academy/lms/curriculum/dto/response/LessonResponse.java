package com.academy.lms.curriculum.dto.response;

import java.util.UUID;

public record LessonResponse(
    UUID id,
    String title,
    String content,
    String videoUrl,
    int position,
    int durationMinutes,
    boolean preview
) {}
