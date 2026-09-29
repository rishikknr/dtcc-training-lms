package com.academy.lms.course.service;

import com.academy.lms.course.repository.CourseRepository;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class SlugService {
  private final CourseRepository courses;

  public SlugService(CourseRepository courses) { this.courses = courses; }

  public String unique(String title) {
    String base = Normalizer.normalize(title, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
    if (base.isBlank()) base = "course";
    String candidate = base;
    int suffix = 2;
    while (courses.existsBySlug(candidate)) candidate = base + "-" + suffix++;
    return candidate;
  }
}
