package com.econest.controller;

import com.econest.entity.Product;
import com.econest.service.CategoryService;
import com.econest.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Product> featured = productService.findFeatured();
        List<Product> newArrivals = productService.findNewArrivals();

        model.addAttribute("featuredProducts", featured);
        model.addAttribute("newArrivals", newArrivals);
        model.addAttribute("categories", categoryService.findActiveCategories());

        return "index";
    }

    @GetMapping("/sustainability")
    public String sustainability() {
        return "sustainability";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @PostMapping("/contact")
    public String handleContact(@RequestParam("name") String name,
                                @RequestParam("email") String email,
                                @RequestParam("subject") String subject,
                                @RequestParam("message") String message,
                                RedirectAttributes redirectAttributes) {
        // In a real application, send an email or save to DB
        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you, " + name + "! Your message has been received. Our eco-support team will respond within 24 hours.");
        return "redirect:/contact";
    }

    @PostMapping("/newsletter")
    public String handleNewsletter(@RequestParam("email") String email,
                                   RedirectAttributes redirectAttributes) {
        if (email == null || !email.contains("@")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid email address.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Welcome to our green community! Check your inbox for 10% off your first order (code: ECO10).");
        }
        return "redirect:/";
    }
}
