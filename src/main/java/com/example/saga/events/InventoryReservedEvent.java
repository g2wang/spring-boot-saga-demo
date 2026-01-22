package com.example.saga.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {
    private String orderId;
    private String reservationId;
    private boolean success;
    private String message;
}

