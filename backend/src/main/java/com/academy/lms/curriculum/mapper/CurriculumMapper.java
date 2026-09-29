package com.academy.lms.curriculum.mapper;

import com.academy.lms.curriculum.dto.response.LessonResponse;
import com.academy.lms.curriculum.dto.response.SectionResponse;
import com.academy.lms.curriculum.entity.CourseSection;
import com.academy.lms.curriculum.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class CurriculumMapper {
  public SectionResponse toSection(CourseSection section, boolean includeProtectedContent) {
    return new SectionResponse(section.getId(), section.getTitle(), section.getPosition(),
        section.getLessons().stream()
            .map(lesson -> toLesson(lesson, includeProtectedContent || lesson.isPreview()))
            .toList());
  }

  public LessonResponse toLesson(Lesson lesson, boolean includeContent) {
    return new LessonResponse(lesson.getId(), lesson.getTitle(),
        includeContent ? lesson.getContent() : null,
        includeContent ? lesson.getVideoUrl() : null,
        lesson.getPosition(), lesson.getDurationMinutes(), lesson.isPreview());
  }
}
