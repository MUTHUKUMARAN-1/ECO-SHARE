package com.econest.controller;

import com.econest.dto.CategoryForm;
import com.econest.entity.Category;
import com.econest.exception.DuplicateResourceException;
import com.econest.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        Map<Long, Long> productCounts = new HashMap<>();
        for (Category c : categories) {
            productCounts.put(c.getId(), categoryService.countProducts(c.getId()));
        }

        model.addAttribute("categories", categories);
        model.addAttribute("productCounts", productCounts);
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        CategoryForm form = new CategoryForm();
        form.setActive(true);
        model.addAttribute("categoryForm", form);
        return "admin/categories/form";
    }

    @PostMapping("/new")
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }

        try {
            categoryService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
            return "redirect:/admin/categories";
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("name", "error.categoryForm", ex.getMessage());
            return "admin/categories/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.findById(id);

        CategoryForm form = new CategoryForm();
        form.setId(category.getId());
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        form.setImageUrl(category.getImageUrl());
        form.setDisplayOrder(category.getDisplayOrder());
        form.setActive(category.isActive());

        model.addAttribute("categoryForm", form);
        return "admin/categories/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }

        try {
            categoryService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            return "redirect:/admin/categories";
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("name", "error.categoryForm", ex.getMessage());
            return "admin/categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
