package com.academy.lms.category.repository;

import com.academy.lms.category.entity.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  boolean existsByNameIgnoreCase(String name);
  boolean existsBySlug(String slug);
  boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
  boolean existsBySlugAndIdNot(String slug, UUID id);
}
