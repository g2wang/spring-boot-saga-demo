package com.example.saga.service;

import com.example.saga.config.KafkaTopicConfig;
import com.example.saga.events.OrderCreatedEvent;
import com.example.saga.events.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @KafkaListener(topics = KafkaTopicConfig.INVENTORY_EVENTS_TOPIC, groupId = "inventory-service-group")
    public void reserveInventory(OrderCreatedEvent event) {
        log.info("Reserving inventory for orderId: {}, productId: {}, quantity: {}", 
                event.getOrderId(), event.getProductId(), event.getQuantity());
        
        try {
            // Simulate inventory check
            Thread.sleep(1000);
            
            // Simulate inventory availability (80% success rate)
            boolean success = Math.random() > 0.2;
            String reservationId = success ? UUID.randomUUID().toString() : null;
            String message = success ? "Inventory reserved" : "Insufficient stock";
            
            InventoryReservedEvent reservedEvent = InventoryReservedEvent.builder()
                    .orderId(event.getOrderId())
                    .reservationId(reservationId)
                    .success(success)
                    .message(message)
                    .build();
            
            kafkaTemplate.send(KafkaTopicConfig.INVENTORY_RESERVED_TOPIC, event.getOrderId(), reservedEvent);
            log.info("Inventory reservation result for orderId: {}, Success: {}", 
                    event.getOrderId(), success);
            
        } catch (InterruptedException e) {
            log.error("Error reserving inventory", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @KafkaListener(topics = KafkaTopicConfig.COMPENSATE_INVENTORY_TOPIC, groupId = "inventory-service-group")
    public void compensateInventory(com.example.saga.events.CompensateInventoryEvent event) {
        log.warn("Compensating inventory for orderId: {}, reservationId: {}", 
                event.getOrderId(), event.getReservationId());
        
        try {
            // Simulate inventory release
            Thread.sleep(500);
            log.info("Inventory released for orderId: {}", event.getOrderId());
        } catch (InterruptedException e) {
            log.error("Error compensating inventory", e);
            Thread.currentThread().interrupt();
        }
    }
}
