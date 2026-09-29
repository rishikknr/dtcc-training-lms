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

  @org.springframework.data.jpa.repository.Query("select count(p) from LessonProgress p "
      + "where p.enrollment.id = :enrollmentId and p.lesson.published = true")
  long countPublishedByEnrollmentId(
      @org.springframework.data.repository.query.Param("enrollmentId") UUID enrollmentId);
}
