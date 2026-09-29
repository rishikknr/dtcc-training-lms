package com.academy.lms.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record LearningCourseResponse(
    UUID courseId,
    String title,
    short progress,
    List<LearningSection> sections
) {
  public record LearningSection(UUID id, String title, int position, List<LearningLesson> lessons) {}
  public record LearningLesson(UUID id, String title, String description, String content,
                               String videoUrl, String resourceUrl, int position,
                               int durationMinutes, boolean completed) {}
}
