package com.econest.service.impl;

import com.econest.dto.CartDto;
import com.econest.dto.CartItemDto;
import com.econest.entity.Cart;
import com.econest.entity.CartItem;
import com.econest.entity.Product;
import com.econest.entity.User;
import com.econest.exception.InsufficientStockException;
import com.econest.exception.ResourceNotFoundException;
import com.econest.repository.CartItemRepository;
import com.econest.repository.CartRepository;
import com.econest.repository.ProductRepository;
import com.econest.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Cart getOrCreateCart(String sessionToken, User user) {
        if (user != null) {
            Optional<Cart> userCart = cartRepository.findByUser(user);
            if (userCart.isPresent()) {
                return userCart.get();
            }
        }

        if (sessionToken != null && !sessionToken.trim().isEmpty()) {
            Optional<Cart> sessionCart = cartRepository.findBySessionToken(sessionToken);
            if (sessionCart.isPresent()) {
                Cart cart = sessionCart.get();
                if (user != null && cart.getUser() == null) {
                    cart.setUser(user);
                    return cartRepository.save(cart);
                }
                return cart;
            }
        }

        Cart newCart = new Cart();
        newCart.setSessionToken(sessionToken != null ? sessionToken : UUID.randomUUID().toString());
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCartDto(String sessionToken, User user, String promoCode) {
        Cart cart = findExistingCart(sessionToken, user);
        CartDto dto = new CartDto();

        if (cart == null || cart.getItems().isEmpty()) {
            dto.recalculate(BigDecimal.ZERO, null);
            return dto;
        }

        List<CartItemDto> itemDtos = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            CartItemDto itemDto = new CartItemDto(
                    p.getId(),
                    p.getName(),
                    p.getSku(),
                    p.getImageUrl(),
                    p.getPrice(), // Fresh price from database
                    item.getQuantity(),
                    p.getStockQuantity()
            );
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);

        BigDecimal discount = BigDecimal.ZERO;
        String validCode = null;
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            String cleanCode = promoCode.trim().toUpperCase();
            BigDecimal subtotal = itemDtos.stream()
                    .map(CartItemDto::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if ("ECO10".equals(cleanCode)) {
                discount = subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
                validCode = cleanCode;
            } else if ("GREEN20".equals(cleanCode)) {
                discount = subtotal.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
                validCode = cleanCode;
            }
        }

        dto.recalculate(discount, validCode);
        return dto;
    }

    @Override
    @Transactional
    public void addItem(String sessionToken, User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isActive()) {
            throw new IllegalStateException("Product is currently unavailable");
        }

        Cart cart = getOrCreateCart(sessionToken, user);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        int currentQtyInCart = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int totalRequested = currentQtyInCart + quantity;

        if (totalRequested > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), totalRequested, product.getStockQuantity());
        }

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(totalRequested);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.getItems().add(newItem);
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void updateItemQuantity(String sessionToken, User user, Long productId, int quantity) {
        Cart cart = findExistingCart(sessionToken, user);
        if (cart == null) {
            return;
        }

        if (quantity <= 0) {
            removeItem(sessionToken, user, productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStockQuantity());
        }

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
                return;
            }
        }
    }

    @Override
    @Transactional
    public void removeItem(String sessionToken, User user, Long productId) {
        Cart cart = findExistingCart(sessionToken, user);
        if (cart == null) {
            return;
        }

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(String sessionToken, User user) {
        Cart cart = findExistingCart(sessionToken, user);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getItemCount(String sessionToken, User user) {
        Cart cart = findExistingCart(sessionToken, user);
        if (cart == null) {
            return 0;
        }
        return cart.getTotalQuantity();
    }

    @Override
    @Transactional
    public void mergeGuestCart(String sessionToken, User user) {
        if (sessionToken == null || user == null) {
            return;
        }

        Optional<Cart> guestCartOpt = cartRepository.findBySessionToken(sessionToken);
        if (!guestCartOpt.isPresent()) {
            return;
        }

        Cart guestCart = guestCartOpt.get();
        if (guestCart.getItems().isEmpty()) {
            return;
        }

        Optional<Cart> userCartOpt = cartRepository.findByUser(user);
        Cart userCart;
        if (!userCartOpt.isPresent()) {
            userCart = guestCart;
            userCart.setUser(user);
            cartRepository.save(userCart);
            return;
        } else {
            userCart = userCartOpt.get();
        }

        if (userCart.getId().equals(guestCart.getId())) {
            return;
        }

        for (CartItem guestItem : guestCart.getItems()) {
            Product product = guestItem.getProduct();
            Optional<CartItem> userItemOpt = userCart.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (userItemOpt.isPresent()) {
                CartItem userItem = userItemOpt.get();
                int combinedQty = userItem.getQuantity() + guestItem.getQuantity();
                userItem.setQuantity(Math.min(combinedQty, product.getStockQuantity()));
            } else {
                CartItem newItem = new CartItem(userCart, product, Math.min(guestItem.getQuantity(), product.getStockQuantity()));
                userCart.getItems().add(newItem);
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    private Cart findExistingCart(String sessionToken, User user) {
        if (user != null) {
            Optional<Cart> userCart = cartRepository.findByUser(user);
            if (userCart.isPresent()) {
                return userCart.get();
            }
        }
        if (sessionToken != null) {
            return cartRepository.findBySessionToken(sessionToken).orElse(null);
        }
        return null;
    }
}
