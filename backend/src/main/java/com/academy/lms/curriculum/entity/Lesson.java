package com.academy.lms.curriculum.entity;

import com.academy.lms.common.domain.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lessons")
public class Lesson extends AuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "section_id", nullable = false)
  private CourseSection section;

  @Column(nullable = false, length = 160) private String title;
  @Column(nullable = false, columnDefinition = "text") private String content;
  @Column(name = "video_url", length = 500) private String videoUrl;
  @Column(nullable = false) private int position;
  @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
  @Column(nullable = false) private boolean preview;

  protected Lesson() {}

  public Lesson(CourseSection section, String title, String content, String videoUrl,
                int position, int durationMinutes, boolean preview) {
    this.section = section;
    this.position = position;
    update(title, content, videoUrl, durationMinutes, preview);
  }

  public CourseSection getSection() { return section; }
  public String getTitle() { return title; }
  public String getContent() { return content; }
  public String getVideoUrl() { return videoUrl; }
  public int getPosition() { return position; }
  public int getDurationMinutes() { return durationMinutes; }
  public boolean isPreview() { return preview; }

  public void update(String title, String content, String videoUrl, int durationMinutes,
                     boolean preview) {
    this.title = title.trim();
    this.content = content.trim();
    this.videoUrl = videoUrl == null || videoUrl.isBlank() ? null : videoUrl.trim();
    this.durationMinutes = durationMinutes;
    this.preview = preview;
  }

  public void reposition(int position) { this.position = position; }
}
