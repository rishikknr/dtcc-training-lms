package com.academy.lms.instructor.entity;

import com.academy.lms.common.domain.AuditedEntity;
import com.academy.lms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "instructor_applications")
public class InstructorApplication extends AuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "applicant_id", nullable = false, unique = true)
  private User applicant;

  @Column(nullable = false, length = 500) private String expertise;
  @Column(nullable = false, length = 2000) private String motivation;
  @Column(name = "portfolio_url", length = 500) private String portfolioUrl;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
  private InstructorApplicationStatus status = InstructorApplicationStatus.PENDING;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private User reviewedBy;
  @Column(name = "review_note", length = 1000) private String reviewNote;
  @Column(name = "reviewed_at") private Instant reviewedAt;

  protected InstructorApplication() {}

  public InstructorApplication(User applicant, String expertise, String motivation,
                               String portfolioUrl) {
    this.applicant = applicant;
    updateSubmission(expertise, motivation, portfolioUrl);
  }

  public User getApplicant() { return applicant; }
  public String getExpertise() { return expertise; }
  public String getMotivation() { return motivation; }
  public String getPortfolioUrl() { return portfolioUrl; }
  public InstructorApplicationStatus getStatus() { return status; }
  public User getReviewedBy() { return reviewedBy; }
  public String getReviewNote() { return reviewNote; }
  public Instant getReviewedAt() { return reviewedAt; }

  public void resubmit(String expertise, String motivation, String portfolioUrl) {
    updateSubmission(expertise, motivation, portfolioUrl);
    status = InstructorApplicationStatus.PENDING;
    reviewedBy = null;
    reviewNote = null;
    reviewedAt = null;
  }

  public void decide(InstructorApplicationStatus status, String note, User reviewer) {
    this.status = status;
    this.reviewNote = note == null || note.isBlank() ? null : note.trim();
    this.reviewedBy = reviewer;
    this.reviewedAt = Instant.now();
  }

  private void updateSubmission(String expertise, String motivation, String portfolioUrl) {
    this.expertise = expertise.trim();
    this.motivation = motivation.trim();
    this.portfolioUrl = portfolioUrl == null || portfolioUrl.isBlank() ? null : portfolioUrl.trim();
  }
}
