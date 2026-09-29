package com.academy.lms.curriculum.mapper;

import com.academy.lms.curriculum.dto.response.LessonResponse;
import com.academy.lms.curriculum.dto.response.SectionResponse;
import com.academy.lms.curriculum.entity.CourseSection;
import com.academy.lms.curriculum.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class CurriculumMapper {
  public SectionResponse toSection(CourseSection section, boolean includeProtectedContent) {
    return toSection(section, includeProtectedContent, includeProtectedContent);
  }

  public SectionResponse toSection(CourseSection section, boolean includeProtectedContent,
                                   boolean includeUnpublished) {
    return new SectionResponse(section.getId(), section.getTitle(), section.getPosition(),
        section.getLessons().stream()
            .filter(lesson -> includeUnpublished || lesson.isPublished())
            .map(lesson -> toLesson(lesson, includeProtectedContent || lesson.isPreview()))
            .toList());
  }

  public LessonResponse toLesson(Lesson lesson, boolean includeContent) {
    return new LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getDescription(),
        includeContent ? lesson.getContent() : null,
        includeContent ? lesson.getVideoUrl() : null,
        includeContent ? lesson.getResourceUrl() : null,
        lesson.getPosition(), lesson.getDurationMinutes(), lesson.isPreview(), lesson.isPublished());
  }
}
