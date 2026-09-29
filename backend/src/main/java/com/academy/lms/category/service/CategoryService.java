package com.academy.lms.category.service;

import com.academy.lms.category.dto.request.CategoryRequest;
import com.academy.lms.category.dto.response.CategoryResponse;
import com.academy.lms.category.entity.Category;
import com.academy.lms.category.mapper.CategoryMapper;
import com.academy.lms.category.repository.CategoryRepository;
import com.academy.lms.common.audit.AuditService;
import com.academy.lms.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
  private final CategoryRepository categories;
  private final CategoryMapper mapper;
  private final AuditService audit;

  public CategoryService(CategoryRepository categories, CategoryMapper mapper, AuditService audit) {
    this.categories = categories;
    this.mapper = mapper;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> list() {
    return categories.findAll(Sort.by("name")).stream().map(mapper::toResponse).toList();
  }

  @Transactional
  public CategoryResponse create(UUID actor, CategoryRequest request, HttpServletRequest http) {
    ensureUnique(request, null);
    Category category = categories.save(new Category(request.name(), request.slug(), request.description()));
    audit.record(actor, "CATEGORY_CREATED", "CATEGORY", category.getId(), http);
    return mapper.toResponse(category);
  }

  @Transactional
  public CategoryResponse update(UUID actor, UUID id, CategoryRequest request,
                                 HttpServletRequest http) {
    ensureUnique(request, id);
    Category category = require(id);
    category.update(request.name(), request.slug(), request.description());
    audit.record(actor, "CATEGORY_UPDATED", "CATEGORY", id, http);
    return mapper.toResponse(category);
  }

  @Transactional
  public void delete(UUID actor, UUID id, HttpServletRequest http) {
    Category category = require(id);
    categories.delete(category);
    audit.record(actor, "CATEGORY_DELETED", "CATEGORY", id, http);
  }

  public Category require(UUID id) {
    return id == null ? null : categories.findById(id)
        .orElseThrow(() -> ApiException.notFound("Category"));
  }

  private void ensureUnique(CategoryRequest request, UUID id) {
    boolean duplicateName = id == null
        ? categories.existsByNameIgnoreCase(request.name())
        : categories.existsByNameIgnoreCaseAndIdNot(request.name(), id);
    boolean duplicateSlug = id == null
        ? categories.existsBySlug(request.slug())
        : categories.existsBySlugAndIdNot(request.slug(), id);
    if (duplicateName || duplicateSlug) throw ApiException.conflict("Category name or slug already exists");
  }
}
