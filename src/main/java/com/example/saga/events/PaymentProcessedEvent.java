package com.example.saga.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String orderId;
    private String paymentId;
    private boolean success;
    private String message;
}
