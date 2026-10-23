package com.econest.dto;

import com.econest.entity.OrderStatus;

import javax.validation.constraints.NotNull;

public class OrderStatusUpdateForm {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    public OrderStatusUpdateForm() {
    }

    public OrderStatusUpdateForm(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
