package com.academy.lms.learning.repository;

import com.academy.lms.learning.entity.LessonProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {
  List<LessonProgress> findByEnrollmentId(UUID enrollmentId);
  Optional<LessonProgress> findByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);
  long countByEnrollmentId(UUID enrollmentId);
}
