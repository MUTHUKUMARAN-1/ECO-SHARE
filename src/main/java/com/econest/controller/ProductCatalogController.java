package com.econest.controller;

import com.econest.entity.Category;
import com.econest.entity.Product;
import com.econest.service.CategoryService;
import com.econest.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
public class ProductCatalogController {

    private static final List<String> POPULAR_TAGS = Arrays.asList(
            "Plastic-Free", "Zero Waste", "100% Organic", "Biodegradable", "Upcycled", "Fair Trade", "Compostable", "Vegan"
    );

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductCatalogController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/shop")
    public String shop(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "tag", required = false) String tag,
            @RequestParam(name = "inStockOnly", required = false) Boolean inStockOnly,
            @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {

        int pageSize = 9;
        Page<Product> productPage = productService.findCatalog(
                keyword, categoryId, minPrice, maxPrice, tag, inStockOnly, sort, page, pageSize);

        Category selectedCategory = null;
        if (categoryId != null) {
            try {
                selectedCategory = categoryService.findById(categoryId);
            } catch (Exception ignored) {
            }
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("inStockOnly", inStockOnly);
        model.addAttribute("sort", sort);

        model.addAttribute("allCategories", categoryService.findActiveCategories());
        model.addAttribute("popularTags", POPULAR_TAGS);

        return "shop/index";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        List<Product> relatedProducts = productService.findRelatedProducts(id);

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);

        return "shop/details";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.findActiveCategories());
        return "shop/categories";
    }

    @GetMapping("/categories/{slug}")
    public String categoryProducts(@PathVariable("slug") String slug) {
        Category category = categoryService.findBySlug(slug);
        return "redirect:/shop?category=" + category.getId();
    }
}
