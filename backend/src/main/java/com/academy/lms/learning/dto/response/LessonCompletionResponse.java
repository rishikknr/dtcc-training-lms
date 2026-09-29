package com.academy.lms.learning.dto.response;

import java.util.UUID;

public record LessonCompletionResponse(UUID lessonId, boolean completed, short courseProgress) {}
