package com.econest.controller;

import com.econest.dto.ProductForm;
import com.econest.entity.Product;
import com.econest.exception.DuplicateResourceException;
import com.econest.service.CategoryService;
import com.econest.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {

        int pageSize = 10;
        Page<Product> productPage = productService.findCatalog(
                keyword, categoryId, null, null, null, null, "newest", page, pageSize);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findAll());

        return "admin/products/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        ProductForm form = new ProductForm();
        form.setActive(true);
        form.setFeatured(false);

        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/products/form";
    }

    @PostMapping("/new")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        try {
            productService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            return "redirect:/admin/products";
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("sku", "error.productForm", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error saving product: " + ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);

        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setSku(product.getSku());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setStockQuantity(product.getStockQuantity());
        form.setImageUrl(product.getImageUrl());
        form.setSustainabilityTags(product.getSustainabilityTags());
        form.setMaterialInfo(product.getMaterialInfo());
        form.setPackagingInfo(product.getPackagingInfo());
        form.setOriginCountry(product.getOriginCountry());
        form.setFeatured(product.isFeatured());
        form.setActive(product.isActive());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);

        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/products/form";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("productForm") ProductForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }

        try {
            productService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/admin/products";
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("sku", "error.productForm", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error updating product: " + ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete product: " + ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product status updated.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/stock")
    public String updateStock(@PathVariable("id") Long id,
                              @RequestParam("stockQuantity") int stock,
                              RedirectAttributes redirectAttributes) {
        try {
            productService.updateStock(id, stock);
            redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update stock: " + ex.getMessage());
        }
        return "redirect:/admin/products";
    }
}
