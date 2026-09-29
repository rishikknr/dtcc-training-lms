package com.academy.lms.course.mapper;

import com.academy.lms.course.dto.response.CourseDetailResponse;
import com.academy.lms.course.dto.response.CourseSummaryResponse;
import com.academy.lms.course.entity.Course;
import com.academy.lms.curriculum.mapper.CurriculumMapper;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
  private final CurriculumMapper curriculum;

  public CourseMapper(CurriculumMapper curriculum) { this.curriculum = curriculum; }

  public CourseSummaryResponse toSummary(Course course) {
    var category = course.getCategory();
    return new CourseSummaryResponse(
        course.getId(), course.getTitle(), course.getSlug(), course.getShortDescription(),
        course.getLevel(), course.getStatus(), course.getThumbnailUrl(), course.getAverageRating(),
        course.getRatingCount(), category == null ? null : category.getId(),
        category == null ? null : category.getName(), course.getInstructor().getId(),
        course.getInstructor().getDisplayName());
  }

  public CourseDetailResponse toDetail(Course course, boolean includeProtectedContent,
                                       boolean enrolled, boolean manageable) {
    var category = course.getCategory();
    return new CourseDetailResponse(
        course.getId(), course.getTitle(), course.getSlug(), course.getShortDescription(),
        course.getDescription(), course.getLevel(), course.getStatus(), course.getThumbnailUrl(),
        course.getPrerequisites(), course.getLearningObjectives(), course.getTags(),
        course.getLanguage(), course.getSections().stream().flatMap(section -> section.getLessons().stream())
            .filter(lesson -> lesson.isPublished()).mapToInt(lesson -> lesson.getDurationMinutes()).sum(),
        course.getAverageRating(), course.getRatingCount(),
        category == null ? null : new CourseDetailResponse.CategoryRef(
            category.getId(), category.getName(), category.getSlug()),
        new CourseDetailResponse.InstructorRef(course.getInstructor().getId(),
            course.getInstructor().getDisplayName()),
        course.getSections().stream()
            .map(section -> curriculum.toSection(section, includeProtectedContent, manageable)).toList(),
        enrolled, manageable, course.getCreatedAt(), course.getUpdatedAt());
  }
}
