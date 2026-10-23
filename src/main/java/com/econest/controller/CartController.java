package com.econest.controller;

import com.econest.dto.CartDto;
import com.econest.entity.User;
import com.econest.exception.InsufficientStockException;
import com.econest.security.CustomUserDetails;
import com.econest.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpServletRequest request,
                           Authentication authentication,
                           @RequestParam(name = "promoCode", required = false) String promoCodeParam,
                           Model model) {
        String sessionToken = getOrCreateSessionToken(request);
        User user = getUser(authentication);

        HttpSession session = request.getSession();
        String promoCode = promoCodeParam != null ? promoCodeParam : (String) session.getAttribute("APPLIED_PROMO");

        CartDto cartDto = cartService.getCartDto(sessionToken, user, promoCode);
        model.addAttribute("cart", cartDto);

        return "cart/index";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(name = "redirect", required = false) String redirectUrl,
                            HttpServletRequest request,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        String sessionToken = getOrCreateSessionToken(request);
        User user = getUser(authentication);

        try {
            cartService.addItem(sessionToken, user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to your eco cart!");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not add item to cart: " + ex.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.trim().isEmpty() && redirectUrl.startsWith("/")) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpServletRequest request,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        String sessionToken = getOrCreateSessionToken(request);
        User user = getUser(authentication);

        try {
            cartService.updateItemQuantity(sessionToken, user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully.");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update quantity: " + ex.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam("productId") Long productId,
                             HttpServletRequest request,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String sessionToken = getOrCreateSessionToken(request);
        User user = getUser(authentication);

        cartService.removeItem(sessionToken, user, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");

        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpServletRequest request,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        String sessionToken = getOrCreateSessionToken(request);
        User user = getUser(authentication);

        cartService.clearCart(sessionToken, user);
        request.getSession().removeAttribute("APPLIED_PROMO");
        redirectAttributes.addFlashAttribute("successMessage", "Cart cleared.");

        return "redirect:/cart";
    }

    @PostMapping("/promo")
    public String applyPromo(@RequestParam("promoCode") String promoCode,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        String cleanCode = promoCode != null ? promoCode.trim().toUpperCase() : "";
        if ("ECO10".equals(cleanCode)) {
            request.getSession().setAttribute("APPLIED_PROMO", cleanCode);
            redirectAttributes.addFlashAttribute("successMessage", "Promo code ECO10 applied: 10% discount on products!");
        } else if ("GREEN20".equals(cleanCode)) {
            request.getSession().setAttribute("APPLIED_PROMO", cleanCode);
            redirectAttributes.addFlashAttribute("successMessage", "Promo code GREEN20 applied: 20% discount on products!");
        } else {
            request.getSession().removeAttribute("APPLIED_PROMO");
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid promo code. Try 'ECO10' for 10% off.");
        }
        return "redirect:/cart";
    }

    private String getOrCreateSessionToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute("SESSION_CART_TOKEN");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("SESSION_CART_TOKEN", token);
        }
        return token;
    }

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }
        return null;
    }
}
