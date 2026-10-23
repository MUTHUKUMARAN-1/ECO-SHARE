package com.econest.entity;

public enum PaymentMethod {
    MOCK_ONLINE("Mock Online Payment"),
    CASH_ON_DELIVERY("Cash on Delivery (Eco-Verified)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
