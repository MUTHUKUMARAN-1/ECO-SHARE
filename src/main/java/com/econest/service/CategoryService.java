package com.econest.service;

import com.econest.dto.CategoryForm;
import com.econest.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findAll();

    List<Category> findActiveCategories();

    Category findById(Long id);

    Category findBySlug(String slug);

    Category create(CategoryForm form);

    Category update(Long id, CategoryForm form);

    void delete(Long id);

    long countProducts(Long categoryId);
}
