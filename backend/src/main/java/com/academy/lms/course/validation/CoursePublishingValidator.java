package com.academy.lms.course.validation;

import com.academy.lms.common.exception.ApiException;
import com.academy.lms.course.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CoursePublishingValidator {
  public void validate(Course course) {
    if (course.getCategory() == null) throw ApiException.conflict("Choose a category before publishing");
    if (course.getThumbnailUrl() == null) throw ApiException.conflict("Add a cover image before publishing");
    if (course.getSections().isEmpty()) throw ApiException.conflict("Add at least one section before publishing");
    boolean hasLesson = course.getSections().stream()
        .flatMap(section -> section.getLessons().stream()).anyMatch(lesson -> lesson.isPublished());
    if (!hasLesson) throw ApiException.conflict("Publish at least one lesson before publishing the course");
  }
}
