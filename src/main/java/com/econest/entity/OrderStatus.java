package com.econest.entity;

public enum OrderStatus {
    PENDING("Pending Confirmation"),
    PROCESSING("Processing & Packing"),
    SHIPPED("In Transit (Eco Courier)"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this == newStatus) {
            return true;
        }
        switch (this) {
            case PENDING:
                return newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING:
                return newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
            case CANCELLED:
            default:
                return false;
        }
    }
}
