package com.example.saga.model;

public enum SagaStatus {
    PENDING,
    ORDER_CREATED,
    PAYMENT_PROCESSED,
    INVENTORY_RESERVED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
