package com.econest.service;

import com.econest.dto.CartDto;
import com.econest.entity.*;
import com.econest.exception.InsufficientStockException;
import com.econest.repository.CartItemRepository;
import com.econest.repository.CartRepository;
import com.econest.repository.ProductRepository;
import com.econest.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Product testProduct;
    private Cart testCart;
    private User testUser;

    @BeforeEach
    void setUp() {
        Category category = new Category("Kitchen", "kitchen", "Desc", "img.jpg", 1);
        category.setId(1L);

        testProduct = new Product(
                "ECO-BOT-01",
                "Stainless Steel Bottle",
                "stainless-steel-bottle",
                "Eco bottle",
                new BigDecimal("25.00"),
                10,
                "bottle.jpg",
                "Zero Waste, Plastic-Free",
                "Steel",
                "Paper",
                "USA",
                category,
                true
        );
        testProduct.setId(100L);
        testProduct.setActive(true);

        testUser = new User("emma@test.com", "pass", "Emma", "123456", Role.ROLE_CUSTOMER);
        testUser.setId(1L);

        testCart = new Cart("sess-123");
        testCart.setId(200L);
    }

    @Test
    @DisplayName("Should add product to cart when stock is available")
    void testAddToCartSuccess() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findBySessionToken("sess-123")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.addItem("sess-123", null, 100L, 2);

        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity());
        verify(cartRepository).save(testCart);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
    void testAddToCartExceedsStock() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findBySessionToken("sess-123")).thenReturn(Optional.of(testCart));

        // Available stock is 10, requesting 15
        assertThrows(InsufficientStockException.class, () -> {
            cartService.addItem("sess-123", null, 100L, 15);
        });

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should correctly calculate subtotal, shipping, and discount with promo code")
    void testCartCalculationsWithPromo() {
        CartItem item = new CartItem(testCart, testProduct, 2); // 2 * $25.00 = $50.00
        testCart.getItems().add(item);

        when(cartRepository.findBySessionToken("sess-123")).thenReturn(Optional.of(testCart));

        // With ECO10 (10% discount on $50.00 = $5.00 discount)
        CartDto dto = cartService.getCartDto("sess-123", null, "ECO10");

        assertEquals(new BigDecimal("50.00"), dto.getSubtotal());
        assertEquals(new BigDecimal("5.00"), dto.getDiscountAmount());
        assertEquals(0, dto.getShippingFee().compareTo(BigDecimal.ZERO), "Free shipping at $50 threshold");
        assertTrue(dto.isQualifiesForFreeShipping());
    }

    @Test
    @DisplayName("Should charge shipping when subtotal is below $50 threshold")
    void testShippingChargeBelowThreshold() {
        CartItem item = new CartItem(testCart, testProduct, 1); // 1 * $25.00 = $25.00
        testCart.getItems().add(item);

        when(cartRepository.findBySessionToken("sess-123")).thenReturn(Optional.of(testCart));

        CartDto dto = cartService.getCartDto("sess-123", null, null);

        assertEquals(new BigDecimal("25.00"), dto.getSubtotal());
        assertEquals(new BigDecimal("4.99"), dto.getShippingFee());
        assertFalse(dto.isQualifiesForFreeShipping());
        assertEquals(new BigDecimal("25.00"), dto.getAmountNeededForFreeShipping());
    }
}
