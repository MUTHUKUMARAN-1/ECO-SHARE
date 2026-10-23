package com.econest.service.impl;

import com.econest.dto.DashboardStatsDto;
import com.econest.entity.Order;
import com.econest.entity.OrderStatus;
import com.econest.entity.Product;
import com.econest.repository.OrderRepository;
import com.econest.repository.ProductRepository;
import com.econest.repository.UserRepository;
import com.econest.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(ProductRepository productRepository,
                                OrderRepository orderRepository,
                                UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalProducts(productRepository.count());
        stats.setLowStockProducts(productRepository.countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(10, 0));
        stats.setOutOfStockProducts(productRepository.countByStockQuantityEquals(0));

        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setProcessingOrders(orderRepository.countByStatus(OrderStatus.PROCESSING));
        stats.setShippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));

        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        stats.setTotalCustomers(userRepository.count());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockAlerts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }
}
