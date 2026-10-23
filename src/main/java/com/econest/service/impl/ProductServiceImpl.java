package com.econest.service.impl;

import com.econest.dto.ProductForm;
import com.econest.entity.Category;
import com.econest.entity.Product;
import com.econest.exception.DuplicateResourceException;
import com.econest.exception.ResourceNotFoundException;
import com.econest.repository.ProductRepository;
import com.econest.service.CategoryService;
import com.econest.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findCatalog(String keyword, Long categoryId, BigDecimal minPrice,
                                     BigDecimal maxPrice, String tag, Boolean inStockOnly,
                                     String sort, int page, int size) {
        Sort sorting;
        if ("price_asc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "price");
        } else if ("name_asc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "name");
        } else if ("name_desc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "name");
        } else {
            sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sorting);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always only active products for the catalog
            predicates.add(cb.isTrue(root.get("active")));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(nameLike, descLike));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (tag != null && !tag.trim().isEmpty()) {
                String tagPattern = "%" + tag.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("sustainabilityTags")), tagPattern));
            }

            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findNewArrivals() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findBySku(String sku) {
        return productRepository.findBySku(sku.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findBySlug(String slug) {
        return productRepository.findBySlug(slug.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findRelatedProducts(Long productId) {
        Product product = findById(productId);
        if (product.getCategory() == null) {
            return Collections.emptyList();
        }
        return productRepository.findTop4ByCategoryAndActiveTrueAndIdNot(product.getCategory(), productId);
    }

    @Override
    @Transactional
    public Product create(ProductForm form) {
        String sku = form.getSku().trim().toUpperCase();
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateResourceException("Product", "SKU", sku);
        }

        String name = form.getName().trim();
        String slug = toSlug(name);
        if (productRepository.existsBySlugIgnoreCase(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Category category = categoryService.findById(form.getCategoryId());

        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setSlug(slug);
        product.setDescription(form.getDescription().trim());
        product.setPrice(form.getPrice());
        product.setStockQuantity(form.getStockQuantity());
        product.setImageUrl(form.getImageUrl().trim());
        product.setSustainabilityTags(form.getSustainabilityTags() != null ? form.getSustainabilityTags().trim() : null);
        product.setMaterialInfo(form.getMaterialInfo() != null ? form.getMaterialInfo().trim() : null);
        product.setPackagingInfo(form.getPackagingInfo() != null ? form.getPackagingInfo().trim() : null);
        product.setOriginCountry(form.getOriginCountry() != null ? form.getOriginCountry().trim() : null);
        product.setFeatured(form.isFeatured());
        product.setActive(form.isActive());
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = findById(id);
        String sku = form.getSku().trim().toUpperCase();

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new DuplicateResourceException("Product", "SKU", sku);
        }

        String name = form.getName().trim();
        if (!product.getName().equalsIgnoreCase(name)) {
            String slug = toSlug(name);
            if (productRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
                slug = slug + "-" + id;
            }
            product.setSlug(slug);
        }

        Category category = categoryService.findById(form.getCategoryId());

        product.setSku(sku);
        product.setName(name);
        product.setDescription(form.getDescription().trim());
        product.setPrice(form.getPrice());
        product.setStockQuantity(form.getStockQuantity());
        product.setImageUrl(form.getImageUrl().trim());
        product.setSustainabilityTags(form.getSustainabilityTags() != null ? form.getSustainabilityTags().trim() : null);
        product.setMaterialInfo(form.getMaterialInfo() != null ? form.getMaterialInfo().trim() : null);
        product.setPackagingInfo(form.getPackagingInfo() != null ? form.getPackagingInfo().trim() : null);
        product.setOriginCountry(form.getOriginCountry() != null ? form.getOriginCountry().trim() : null);
        product.setFeatured(form.isFeatured());
        product.setActive(form.isActive());
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Product product = findById(id);
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateStock(Long id, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        Product product = findById(id);
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findLowStock(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return productRepository.countByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStock(int threshold) {
        return productRepository.countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(threshold, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOutOfStock() {
        return productRepository.countByStockQuantityEquals(0);
    }

    private String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
