package com.econest.repository;

import com.econest.entity.Category;
import com.econest.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySlug(String slug);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByFeaturedTrueAndActiveTrue();

    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    long countByActiveTrue();

    long countByStockQuantityLessThanEqual(int maxStock);

    long countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(int maxStock, int minStock);

    long countByStockQuantityEquals(int stock);

    long countByCategory(Category category);

    List<Product> findTop4ByCategoryAndActiveTrueAndIdNot(Category category, Long id);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(int threshold);
}
