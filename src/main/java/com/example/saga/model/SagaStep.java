package com.example.saga.model;

public enum SagaStep {
    CREATE_ORDER,
    PROCESS_PAYMENT,
    RESERVE_INVENTORY,
    COMPLETE_ORDER
}
