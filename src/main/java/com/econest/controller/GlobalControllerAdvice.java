package com.econest.controller;

import com.econest.entity.Category;
import com.econest.entity.User;
import com.econest.security.CustomUserDetails;
import com.econest.service.CartService;
import com.econest.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final CategoryService categoryService;

    public GlobalControllerAdvice(CartService cartService, CategoryService categoryService) {
        this.cartService = cartService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }
        return null;
    }

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpServletRequest request, Authentication authentication) {
        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }

        HttpSession session = request.getSession(true);
        String sessionToken = (String) session.getAttribute("SESSION_CART_TOKEN");
        if (sessionToken == null) {
            sessionToken = UUID.randomUUID().toString();
            session.setAttribute("SESSION_CART_TOKEN", sessionToken);
        }

        return cartService.getItemCount(sessionToken, user);
    }

    @ModelAttribute("navCategories")
    public List<Category> getNavCategories() {
        return categoryService.findActiveCategories();
    }
}
