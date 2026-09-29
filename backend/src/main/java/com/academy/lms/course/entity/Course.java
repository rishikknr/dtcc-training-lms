package com.academy.lms.course.entity;

import com.academy.lms.category.entity.Category;
import com.academy.lms.common.domain.AuditedEntity;
import com.academy.lms.curriculum.entity.CourseSection;
import com.academy.lms.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course extends AuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "instructor_id", nullable = false)
  private User instructor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(nullable = false, length = 160) private String title;
  @Column(nullable = false, unique = true, length = 190) private String slug;
  @Column(name = "short_description", nullable = false, length = 300) private String shortDescription;
  @Column(nullable = false, columnDefinition = "text") private String description;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CourseLevel level;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CourseStatus status = CourseStatus.DRAFT;
  @Column(name = "thumbnail_url", length = 500) private String thumbnailUrl;
  @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
  private BigDecimal averageRating = BigDecimal.ZERO;
  @Column(name = "rating_count", nullable = false) private int ratingCount;
  @Column(name = "published_at") private Instant publishedAt;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position ASC")
  private List<CourseSection> sections = new ArrayList<>();

  protected Course() {}

  public Course(User instructor, Category category, String title, String slug,
                String shortDescription, String description, CourseLevel level,
                String thumbnailUrl) {
    this.instructor = instructor;
    this.slug = slug;
    update(category, title, shortDescription, description, level, thumbnailUrl);
  }

  public User getInstructor() { return instructor; }
  public Category getCategory() { return category; }
  public String getTitle() { return title; }
  public String getSlug() { return slug; }
  public String getShortDescription() { return shortDescription; }
  public String getDescription() { return description; }
  public CourseLevel getLevel() { return level; }
  public CourseStatus getStatus() { return status; }
  public String getThumbnailUrl() { return thumbnailUrl; }
  public BigDecimal getAverageRating() { return averageRating; }
  public int getRatingCount() { return ratingCount; }
  public Instant getPublishedAt() { return publishedAt; }
  public List<CourseSection> getSections() { return List.copyOf(sections); }

  public void update(Category category, String title, String shortDescription,
                     String description, CourseLevel level, String thumbnailUrl) {
    this.category = category;
    this.title = title.trim();
    this.shortDescription = shortDescription.trim();
    this.description = description.trim();
    this.level = level;
    this.thumbnailUrl = normalizeNullable(thumbnailUrl);
  }

  public void changeSlug(String slug) { this.slug = slug; }
  public void assignInstructor(User instructor) { this.instructor = instructor; }

  public void changeStatus(CourseStatus status) {
    this.status = status;
    if (status == CourseStatus.PUBLISHED && publishedAt == null) publishedAt = Instant.now();
  }

  public void updateRating(BigDecimal average, int count) {
    this.averageRating = average;
    this.ratingCount = count;
  }

  public void addSection(CourseSection section) { sections.add(section); }

  private String normalizeNullable(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
