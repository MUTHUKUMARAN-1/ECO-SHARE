package com.econest.service.impl;

import com.econest.dto.CategoryForm;
import com.econest.entity.Category;
import com.econest.exception.DuplicateResourceException;
import com.econest.exception.ResourceNotFoundException;
import com.econest.repository.CategoryRepository;
import com.econest.repository.ProductRepository;
import com.econest.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
    }

    @Override
    @Transactional
    public Category create(CategoryForm form) {
        String name = form.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category", "name", name);
        }

        String slug = toSlug(name);
        if (categoryRepository.existsBySlugIgnoreCase(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(form.getDescription() != null ? form.getDescription().trim() : null);
        category.setImageUrl(form.getImageUrl().trim());
        category.setDisplayOrder(form.getDisplayOrder());
        category.setActive(form.isActive());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryForm form) {
        Category category = findById(id);
        String name = form.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Category", "name", name);
        }

        if (!category.getName().equalsIgnoreCase(name)) {
            String slug = toSlug(name);
            if (categoryRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
                slug = slug + "-" + id;
            }
            category.setSlug(slug);
        }

        category.setName(name);
        category.setDescription(form.getDescription() != null ? form.getDescription().trim() : null);
        category.setImageUrl(form.getImageUrl().trim());
        category.setDisplayOrder(form.getDisplayOrder());
        category.setActive(form.isActive());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        long productCount = productRepository.countByCategory(category);
        if (productCount > 0) {
            throw new IllegalStateException(String.format(
                    "Cannot delete category '%s' because %d product(s) are associated with it. Please reassign or delete these products first.",
                    category.getName(), productCount));
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts(Long categoryId) {
        Category category = findById(categoryId);
        return productRepository.countByCategory(category);
    }

    private String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
