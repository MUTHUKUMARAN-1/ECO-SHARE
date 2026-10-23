package com.econest.service.impl;

import com.econest.dto.CartDto;
import com.econest.dto.CartItemDto;
import com.econest.dto.CheckoutForm;
import com.econest.entity.*;
import com.econest.exception.InsufficientStockException;
import com.econest.exception.InvalidOrderStateException;
import com.econest.exception.ResourceNotFoundException;
import com.econest.repository.OrderRepository;
import com.econest.repository.ProductRepository;
import com.econest.service.CartService;
import com.econest.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public Order placeOrder(String sessionToken, User user, CheckoutForm form) {
        CartDto cartDto = cartService.getCartDto(sessionToken, user, form.getPromoCode());

        if (cartDto.isEmpty()) {
            throw new IllegalStateException("Your cart is empty. Please add items before checking out.");
        }

        // Validate stock for all items
        for (CartItemDto itemDto : cartDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));

            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new InsufficientStockException(product.getName(), itemDto.getQuantity(), product.getStockQuantity());
            }
        }

        // Deduct inventory
        for (CartItemDto itemDto : cartDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId()).get();
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepository.save(product);
        }

        // Generate Order Reference: ECO-YYYY-XXXXXX
        String refSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        String orderReference = "ECO-" + Year.now().getValue() + "-" + refSuffix;

        Order order = new Order();
        order.setOrderReference(orderReference);
        order.setUser(user);
        order.setCustomerName(form.getCustomerName().trim());
        order.setCustomerEmail(form.getCustomerEmail().trim().toLowerCase());
        order.setCustomerPhone(form.getCustomerPhone().trim());
        order.setShippingAddress(form.getShippingAddress().trim());
        order.setCity(form.getCity().trim());
        order.setState(form.getState() != null ? form.getState().trim() : null);
        order.setPostalCode(form.getPostalCode().trim());
        order.setCountry(form.getCountry().trim());
        order.setNotes(form.getNotes() != null ? form.getNotes().trim() : null);

        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(form.getPaymentMethod());

        if (form.getPaymentMethod() == PaymentMethod.MOCK_ONLINE) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        order.setSubtotal(cartDto.getSubtotal());
        order.setShippingFee(cartDto.getShippingFee());
        order.setDiscountAmount(cartDto.getDiscountAmount());
        order.setTotalAmount(cartDto.getTotalAmount());

        // Snapshot line items
        for (CartItemDto itemDto : cartDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId()).orElse(null);
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    itemDto.getProductName(),
                    itemDto.getSku(),
                    itemDto.getUnitPrice(),
                    itemDto.getQuantity(),
                    itemDto.getSubtotal()
            );
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful transaction
        cartService.clearCart(sessionToken, user);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByReference(String orderReference) {
        return orderRepository.findByOrderReference(orderReference.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "reference", orderReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderForUser(Long id, User user) {
        Order order = getOrderById(id);
        if (user == null) {
            throw new AccessDeniedException("You must be logged in to view order details.");
        }
        if (user.getRole() == Role.ROLE_ADMIN) {
            return order;
        }
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(User user) {
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        if (status != null) {
            return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            return order;
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(currentStatus, newStatus);
        }

        // If cancelled, restock products
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        // If delivered and cash on delivery, mark as paid
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
