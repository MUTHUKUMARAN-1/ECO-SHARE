package com.econest.service;

import com.econest.dto.CartDto;
import com.econest.entity.Cart;
import com.econest.entity.User;

public interface CartService {

    Cart getOrCreateCart(String sessionToken, User user);

    CartDto getCartDto(String sessionToken, User user, String promoCode);

    void addItem(String sessionToken, User user, Long productId, int quantity);

    void updateItemQuantity(String sessionToken, User user, Long productId, int quantity);

    void removeItem(String sessionToken, User user, Long productId);

    void clearCart(String sessionToken, User user);

    int getItemCount(String sessionToken, User user);

    void mergeGuestCart(String sessionToken, User user);
}
