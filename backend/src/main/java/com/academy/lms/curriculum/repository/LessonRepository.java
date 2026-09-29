package com.academy.lms.curriculum.repository;

import com.academy.lms.curriculum.entity.Lesson;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
  List<Lesson> findBySectionIdOrderByPosition(UUID sectionId);
  long countBySectionId(UUID sectionId);
  long countBySectionCourseId(UUID courseId);
}
