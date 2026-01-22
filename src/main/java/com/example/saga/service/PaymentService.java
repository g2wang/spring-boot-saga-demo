package com.example.saga.service;

import com.example.saga.events.OrderCreatedEvent;
import com.example.saga.events.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @KafkaListener(topics = "payment-events", groupId = "payment-service-group")
    public void processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for orderId: {}", event.getOrderId());
        
        try {
            // Simulate payment processing
            Thread.sleep(1000);
            
            // Simulate payment success (90% success rate)
            boolean success = Math.random() > 0.1;
            
            String paymentId = success ? UUID.randomUUID().toString() : null;
            String message = success ? "Payment successful" : "Insufficient funds";
            
            PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                    .orderId(event.getOrderId())
                    .paymentId(paymentId)
                    .success(success)
                    .message(message)
                    .build();
            
            kafkaTemplate.send("payment-processed", event.getOrderId(), processedEvent);
            log.info("Payment processed for orderId: {}, Success: {}", event.getOrderId(), success);
            
        } catch (InterruptedException e) {
            log.error("Error processing payment", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @KafkaListener(topics = "compensate-payment", groupId = "payment-service-group")
    public void compensatePayment(com.example.saga.events.CompensatePaymentEvent event) {
        log.warn("Compensating payment for orderId: {}, paymentId: {}", 
                event.getOrderId(), event.getPaymentId());
        
        try {
            // Simulate refund processing
            Thread.sleep(500);
            log.info("Payment refunded for orderId: {}", event.getOrderId());
        } catch (InterruptedException e) {
            log.error("Error compensating payment", e);
            Thread.currentThread().interrupt();
        }
    }
}

