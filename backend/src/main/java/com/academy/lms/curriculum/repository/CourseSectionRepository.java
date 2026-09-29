package com.academy.lms.curriculum.repository;

import com.academy.lms.curriculum.entity.CourseSection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {
  List<CourseSection> findByCourseIdOrderByPosition(UUID courseId);
  long countByCourseId(UUID courseId);
}
