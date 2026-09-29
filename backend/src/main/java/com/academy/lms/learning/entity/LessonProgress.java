package com.academy.lms.learning.entity;

import com.academy.lms.curriculum.entity.Lesson;
import com.academy.lms.enrollment.entity.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_progress", uniqueConstraints = @UniqueConstraint(columnNames = {
    "enrollment_id", "lesson_id"}))
public class LessonProgress {
  @Id private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "enrollment_id", nullable = false) private Enrollment enrollment;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "lesson_id", nullable = false) private Lesson lesson;
  @Column(name = "completed_at", nullable = false) private Instant completedAt;

  protected LessonProgress() {}

  public LessonProgress(Enrollment enrollment, Lesson lesson) {
    this.id = UUID.randomUUID();
    this.enrollment = enrollment;
    this.lesson = lesson;
    this.completedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public Enrollment getEnrollment() { return enrollment; }
  public Lesson getLesson() { return lesson; }
  public Instant getCompletedAt() { return completedAt; }
}
