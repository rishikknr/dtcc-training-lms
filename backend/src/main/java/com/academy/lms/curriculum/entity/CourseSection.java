package com.academy.lms.curriculum.entity;

import com.academy.lms.common.domain.AuditedEntity;
import com.academy.lms.course.entity.Course;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_sections")
public class CourseSection extends AuditedEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(nullable = false, length = 160) private String title;
  @Column(nullable = false) private int position;

  @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position ASC")
  private List<Lesson> lessons = new ArrayList<>();

  protected CourseSection() {}

  public CourseSection(Course course, String title, int position) {
    this.course = course;
    this.title = title.trim();
    this.position = position;
  }

  public Course getCourse() { return course; }
  public String getTitle() { return title; }
  public int getPosition() { return position; }
  public List<Lesson> getLessons() { return List.copyOf(lessons); }
  public void rename(String title) { this.title = title.trim(); }
  public void reposition(int position) { this.position = position; }
  public void addLesson(Lesson lesson) { lessons.add(lesson); }
}
