package com.academy.lms.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
  @Id private UUID id;
  @Column(nullable = false, unique = true, length = 80) private String name;
  @Column(nullable = false, unique = true, length = 100) private String slug;
  @Column(length = 300) private String description;
  @Column(name = "created_at", nullable = false) private Instant createdAt;

  protected Category() {}

  public Category(String name, String slug, String description) {
    this.id = UUID.randomUUID();
    update(name, slug, description);
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getName() { return name; }
  public String getSlug() { return slug; }
  public String getDescription() { return description; }
  public Instant getCreatedAt() { return createdAt; }

  public void update(String name, String slug, String description) {
    this.name = name.trim();
    this.slug = slug.trim();
    this.description = description == null || description.isBlank() ? null : description.trim();
  }
}
