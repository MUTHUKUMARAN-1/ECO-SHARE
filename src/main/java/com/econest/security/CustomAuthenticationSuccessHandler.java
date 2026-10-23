package com.econest.security;

import com.econest.entity.Role;
import com.econest.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final CartService cartService;

    public CustomAuthenticationSuccessHandler(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Merge guest cart if sessionToken exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute("SESSION_CART_TOKEN");
            if (sessionToken != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                cartService.mergeGuestCart(sessionToken, userDetails.getUser());
            }
        }

        // Determine redirect by role
        boolean isAdmin = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (Role.ROLE_ADMIN.name().equals(authority.getAuthority())) {
                isAdmin = true;
                break;
            }
        }

        if (isAdmin) {
            getRedirectStrategy().sendRedirect(request, response, "/admin");
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
