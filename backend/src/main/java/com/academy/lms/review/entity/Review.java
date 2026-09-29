package com.academy.lms.review.entity;

import com.academy.lms.common.domain.AuditedEntity;
import com.academy.lms.course.entity.Course;
import com.academy.lms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {
    "student_id", "course_id"}))
public class Review extends AuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false) private User student;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false) private Course course;
  @Column(nullable = false) private short rating;
  @Column(nullable = false, length = 2000) private String comment;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
  private ReviewStatus status = ReviewStatus.PUBLISHED;
  @Column(name = "moderation_reason", length = 500) private String moderationReason;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "moderated_by") private User moderatedBy;
  @Column(name = "moderated_at") private Instant moderatedAt;

  protected Review() {}

  public Review(User student, Course course, short rating, String comment) {
    this.student = student;
    this.course = course;
    update(rating, comment);
  }

  public User getStudent() { return student; }
  public Course getCourse() { return course; }
  public short getRating() { return rating; }
  public String getComment() { return comment; }
  public ReviewStatus getStatus() { return status; }
  public String getModerationReason() { return moderationReason; }
  public User getModeratedBy() { return moderatedBy; }
  public Instant getModeratedAt() { return moderatedAt; }

  public void update(short rating, String comment) {
    this.rating = rating;
    this.comment = comment.trim();
  }

  public void moderate(ReviewStatus status, String reason, User moderator) {
    this.status = status;
    this.moderationReason = status == ReviewStatus.HIDDEN && reason != null ? reason.trim() : null;
    this.moderatedBy = moderator;
    this.moderatedAt = Instant.now();
  }
}
