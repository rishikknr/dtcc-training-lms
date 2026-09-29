package com.academy.lms.enrollment.entity;

import com.academy.lms.course.entity.Course;
import com.academy.lms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {
    "student_id", "course_id"}))
public class Enrollment {
  @Id private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false) private User student;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false) private Course course;
  @Column(nullable = false) private short progress;
  @Column(name = "enrolled_at", nullable = false) private Instant enrolledAt;
  @Column(name = "completed_at") private Instant completedAt;
  @Column(name = "last_accessed_at") private Instant lastAccessedAt;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
  private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
  @Column(name = "cancelled_at") private Instant cancelledAt;
  @Version private long version;

  protected Enrollment() {}

  public Enrollment(User student, Course course) {
    this.id = UUID.randomUUID();
    this.student = student;
    this.course = course;
    this.enrolledAt = Instant.now();
    this.lastAccessedAt = enrolledAt;
  }

  public UUID getId() { return id; }
  public User getStudent() { return student; }
  public Course getCourse() { return course; }
  public short getProgress() { return progress; }
  public Instant getEnrolledAt() { return enrolledAt; }
  public Instant getCompletedAt() { return completedAt; }
  public Instant getLastAccessedAt() { return lastAccessedAt; }
  public EnrollmentStatus getStatus() { return status; }
  public Instant getCancelledAt() { return cancelledAt; }

  public void updateProgress(int percentage) {
    this.progress = (short) Math.max(0, Math.min(100, percentage));
    this.lastAccessedAt = Instant.now();
    this.completedAt = progress == 100 ? (completedAt == null ? Instant.now() : completedAt) : null;
  }

  public void cancel() {
    status = EnrollmentStatus.CANCELLED;
    cancelledAt = Instant.now();
  }

  public void reactivate() {
    status = EnrollmentStatus.ACTIVE;
    cancelledAt = null;
    lastAccessedAt = Instant.now();
  }
}
