package com.econest.entity;

public enum PaymentStatus {
    PENDING("Payment Pending"),
    PAID("Paid"),
    REFUNDED("Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
