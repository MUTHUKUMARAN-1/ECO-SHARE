package com.econest.controller;

import com.econest.dto.DashboardStatsDto;
import com.econest.entity.Order;
import com.econest.entity.Product;
import com.econest.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getStats();
        List<Order> recentOrders = dashboardService.getRecentOrders();
        List<Product> lowStockAlerts = dashboardService.getLowStockAlerts(10);

        model.addAttribute("stats", stats);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("lowStockAlerts", lowStockAlerts);

        return "admin/dashboard";
    }
}
