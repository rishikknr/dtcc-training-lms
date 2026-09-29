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
  @Column(length = 500) private String description;
  @Column(nullable = false, columnDefinition = "text") private String content;
  @Column(name = "video_url", length = 500) private String videoUrl;
  @Column(name = "resource_url", length = 500) private String resourceUrl;
  @Column(nullable = false) private int position;
  @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
  @Column(nullable = false) private boolean preview;
  @Column(nullable = false) private boolean published = true;

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
  public String getDescription() { return description; }
  public String getVideoUrl() { return videoUrl; }
  public String getResourceUrl() { return resourceUrl; }
  public int getPosition() { return position; }
  public int getDurationMinutes() { return durationMinutes; }
  public boolean isPreview() { return preview; }
  public boolean isPublished() { return published; }

  public void update(String title, String content, String videoUrl, int durationMinutes,
                     boolean preview) {
    update(title, null, content, videoUrl, null, durationMinutes, preview, true);
  }

  public void update(String title, String description, String content, String videoUrl,
                     String resourceUrl, int durationMinutes, boolean preview, boolean published) {
    this.title = title.trim();
    this.description = normalizeNullable(description);
    this.content = content.trim();
    this.videoUrl = normalizeNullable(videoUrl);
    this.resourceUrl = normalizeNullable(resourceUrl);
    this.durationMinutes = durationMinutes;
    this.preview = preview;
    this.published = published;
  }

  public void reposition(int position) { this.position = position; }

  private String normalizeNullable(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
