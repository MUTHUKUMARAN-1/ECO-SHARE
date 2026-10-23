package com.econest.service;

import com.econest.dto.ProductForm;
import com.econest.entity.Product;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Page<Product> findCatalog(String keyword, Long categoryId, BigDecimal minPrice,
                              BigDecimal maxPrice, String tag, Boolean inStockOnly,
                              String sort, int page, int size);

    List<Product> findFeatured();

    List<Product> findNewArrivals();

    Product findById(Long id);

    Product findBySku(String sku);

    Product findBySlug(String slug);

    List<Product> findRelatedProducts(Long productId);

    Product create(ProductForm form);

    Product update(Long id, ProductForm form);

    void delete(Long id);

    void toggleStatus(Long id);

    void updateStock(Long id, int newStock);

    List<Product> findLowStock(int threshold);

    long countTotalProducts();

    long countActive();

    long countLowStock(int threshold);

    long countOutOfStock();
}
