package com.econest.controller;

import com.econest.dto.RegisterForm;
import com.econest.entity.Order;
import com.econest.entity.User;
import com.econest.exception.DuplicateResourceException;
import com.econest.security.CustomUserDetails;
import com.econest.service.OrderService;
import com.econest.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final OrderService orderService;

    public AuthController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model,
                            Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been securely logged out.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return "redirect:/";
        }

        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerForm") RegisterForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerForm", "Passwords do not match");
            return "auth/register";
        }

        try {
            userService.register(form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please sign in with your credentials.");
            return "redirect:/login";
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("email", "error.registerForm", "An account with this email already exists.");
            return "auth/register";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Registration failed: " + ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/account")
    public String accountDashboard(Authentication authentication, Model model) {
        User user = getUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersForUser(user);
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);

        return "account/index";
    }

    @PostMapping("/account/profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "phone", required = false) String phone,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = getUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            userService.updateProfile(user.getId(), fullName, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + ex.getMessage());
        }

        return "redirect:/account";
    }

    @GetMapping("/account/orders/{id}")
    public String viewCustomerOrder(@PathVariable("id") Long id,
                                    Authentication authentication,
                                    Model model) {
        User user = getUser(authentication);
        Order order = orderService.getOrderForUser(id, user);

        model.addAttribute("order", order);
        return "account/order-details";
    }

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }
        return null;
    }
}
