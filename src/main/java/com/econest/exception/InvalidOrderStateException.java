package com.econest.exception;

import com.econest.entity.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus newStatus) {
        super(String.format("Cannot transition order from %s to %s",
                currentStatus.getDisplayName(), newStatus.getDisplayName()));
    }
}
