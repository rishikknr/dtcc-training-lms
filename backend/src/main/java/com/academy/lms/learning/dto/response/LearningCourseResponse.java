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
  public record LearningLesson(UUID id, String title, String content, String videoUrl, int position,
                               int durationMinutes, boolean completed) {}
}
