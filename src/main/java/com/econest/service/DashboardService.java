package com.econest.service;

import com.econest.dto.DashboardStatsDto;
import com.econest.entity.Order;
import com.econest.entity.Product;

import java.util.List;

public interface DashboardService {

    DashboardStatsDto getStats();

    List<Order> getRecentOrders();

    List<Product> getLowStockAlerts(int threshold);
}
