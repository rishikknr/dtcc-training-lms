package com.academy.lms.category.mapper;

import com.academy.lms.category.dto.response.CategoryResponse;
import com.academy.lms.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
  public CategoryResponse toResponse(Category category) {
    return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
        category.getDescription());
  }
}
