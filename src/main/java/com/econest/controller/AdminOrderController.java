package com.econest.controller;

import com.econest.dto.OrderStatusUpdateForm;
import com.econest.entity.Order;
import com.econest.entity.OrderStatus;
import com.econest.exception.InvalidOrderStateException;
import com.econest.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String listOrders(
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {

        int pageSize = 12;
        Page<Order> orderPage = orderService.findAllOrders(status, page, pageSize);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());

        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("statusForm", new OrderStatusUpdateForm(order.getStatus()));
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/orders/details";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long id,
                                    @ModelAttribute("statusForm") OrderStatusUpdateForm form,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order updated = orderService.updateOrderStatus(id, form.getStatus());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + updated.getOrderReference() + " status updated to " + updated.getStatus().getDisplayName());
        } catch (InvalidOrderStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update order status: " + ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
