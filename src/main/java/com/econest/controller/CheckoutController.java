package com.econest.controller;

import com.econest.dto.CartDto;
import com.econest.dto.CheckoutForm;
import com.econest.entity.Order;
import com.econest.entity.User;
import com.econest.exception.InsufficientStockException;
import com.econest.security.CustomUserDetails;
import com.econest.service.CartService;
import com.econest.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpServletRequest request,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String sessionToken = (String) request.getSession().getAttribute("SESSION_CART_TOKEN");
        User user = getUser(authentication);
        String promoCode = (String) request.getSession().getAttribute("APPLIED_PROMO");

        CartDto cartDto = cartService.getCartDto(sessionToken, user, promoCode);
        if (cartDto.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add products before checkout.");
            return "redirect:/cart";
        }

        if (cartDto.isHasStockIssues()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Some items in your cart exceed available stock. Please adjust quantities.");
            return "redirect:/cart";
        }

        CheckoutForm form = new CheckoutForm();
        if (user != null) {
            form.setCustomerName(user.getFullName());
            form.setCustomerEmail(user.getEmail());
            form.setCustomerPhone(user.getPhone());
        }
        form.setPromoCode(promoCode);

        model.addAttribute("checkoutForm", form);
        model.addAttribute("cart", cartDto);

        return "checkout/index";
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                             BindingResult bindingResult,
                             HttpServletRequest request,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String sessionToken = (String) request.getSession().getAttribute("SESSION_CART_TOKEN");
        User user = getUser(authentication);
        String promoCode = (String) request.getSession().getAttribute("APPLIED_PROMO");

        CartDto cartDto = cartService.getCartDto(sessionToken, user, promoCode);

        if (cartDto.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty.");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cartDto);
            return "checkout/index";
        }

        try {
            form.setPromoCode(promoCode);
            Order placedOrder = orderService.placeOrder(sessionToken, user, form);

            // Clear session promo
            request.getSession().removeAttribute("APPLIED_PROMO");

            return "redirect:/orders/" + placedOrder.getOrderReference() + "/confirmation";
        } catch (InsufficientStockException ex) {
            model.addAttribute("cart", cartDto);
            model.addAttribute("errorMessage", ex.getMessage());
            return "checkout/index";
        } catch (Exception ex) {
            model.addAttribute("cart", cartDto);
            model.addAttribute("errorMessage", "Order failed: " + ex.getMessage());
            return "checkout/index";
        }
    }

    @GetMapping("/orders/{orderRef}/confirmation")
    public String orderConfirmation(@PathVariable("orderRef") String orderRef, Model model) {
        Order order = orderService.getOrderByReference(orderRef);
        model.addAttribute("order", order);
        return "checkout/confirmation";
    }

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }
        return null;
    }
}
