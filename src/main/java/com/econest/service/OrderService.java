package com.econest.service;

import com.econest.dto.CheckoutForm;
import com.econest.entity.Order;
import com.econest.entity.OrderStatus;
import com.econest.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Order placeOrder(String sessionToken, User user, CheckoutForm form);

    Order getOrderByReference(String orderReference);

    Order getOrderById(Long id);

    Order getOrderForUser(Long id, User user);

    List<Order> getOrdersForUser(User user);

    Page<Order> findAllOrders(OrderStatus status, int page, int size);

    List<Order> findRecentOrders();

    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
}
