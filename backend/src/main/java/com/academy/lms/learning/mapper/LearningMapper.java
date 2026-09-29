package com.academy.lms.learning.mapper;

import com.academy.lms.enrollment.entity.Enrollment;
import com.academy.lms.learning.dto.response.LearningCourseResponse;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LearningMapper {
  public LearningCourseResponse toResponse(Enrollment enrollment, Set<UUID> completedLessonIds) {
    var course = enrollment.getCourse();
    return new LearningCourseResponse(course.getId(), course.getTitle(), enrollment.getProgress(),
        course.getSections().stream().map(section -> new LearningCourseResponse.LearningSection(
            section.getId(), section.getTitle(), section.getPosition(),
            section.getLessons().stream().map(lesson -> new LearningCourseResponse.LearningLesson(
                lesson.getId(), lesson.getTitle(), lesson.getContent(), lesson.getVideoUrl(),
                lesson.getPosition(), lesson.getDurationMinutes(),
                completedLessonIds.contains(lesson.getId()))).toList())).toList());
  }
}
