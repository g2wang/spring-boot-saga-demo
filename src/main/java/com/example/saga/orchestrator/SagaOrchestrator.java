package com.example.saga.orchestrator;

import com.example.saga.dto.OrderRequest;
import com.example.saga.events.*;
import com.example.saga.model.OrderSaga;
import com.example.saga.model.SagaStatus;
import com.example.saga.model.SagaStep;
import com.example.saga.repository.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {
    
    private final OrderSagaRepository sagaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_TOPIC = "order-events";
    private static final String PAYMENT_TOPIC = "payment-events";
    private static final String INVENTORY_TOPIC = "inventory-events";
    private static final String COMPENSATE_PAYMENT_TOPIC = "compensate-payment";
    private static final String COMPENSATE_INVENTORY_TOPIC = "compensate-inventory";
    
    @Transactional
    public OrderSaga startSaga(OrderRequest request) {
        log.info("Starting saga for order request: {}", request);
        
        String orderId = UUID.randomUUID().toString();
        
        OrderSaga saga = OrderSaga.builder()
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .status(SagaStatus.PENDING)
                .currentStep(SagaStep.CREATE_ORDER)
                .build();
        
        saga = sagaRepository.save(saga);
        
        // Send order created event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .build();
        
        kafkaTemplate.send(ORDER_TOPIC, orderId, event);
        log.info("Order created event sent for orderId: {}", orderId);
        
        updateSagaStatus(saga, SagaStatus.ORDER_CREATED, SagaStep.PROCESS_PAYMENT);
        
        // Proceed to payment
        processPayment(saga);
        
        return saga;
    }
    
    private void processPayment(OrderSaga saga) {
        log.info("Processing payment for orderId: {}", saga.getOrderId());
        kafkaTemplate.send(PAYMENT_TOPIC, saga.getOrderId(), 
            OrderCreatedEvent.builder()
                .orderId(saga.getOrderId())
                .customerId(saga.getCustomerId())
                .amount(saga.getAmount())
                .build());
    }
    
    @KafkaListener(topics = "payment-processed", groupId = "saga-group")
    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received payment processed event: {}", event);
        
        OrderSaga saga = sagaRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Saga not found for orderId: " + event.getOrderId()));
        
        if (event.isSuccess()) {
            saga.setPaymentId(event.getPaymentId());
            updateSagaStatus(saga, SagaStatus.PAYMENT_PROCESSED, SagaStep.RESERVE_INVENTORY);
            
            // Proceed to inventory reservation
            reserveInventory(saga);
        } else {
            log.error("Payment failed for orderId: {}", event.getOrderId());
            updateSagaStatus(saga, SagaStatus.FAILED, saga.getCurrentStep());
        }
    }
    
    private void reserveInventory(OrderSaga saga) {
        log.info("Reserving inventory for orderId: {}", saga.getOrderId());
        kafkaTemplate.send(INVENTORY_TOPIC, saga.getOrderId(),
            OrderCreatedEvent.builder()
                .orderId(saga.getOrderId())
                .productId(saga.getProductId())
                .quantity(saga.getQuantity())
                .build());
    }
    
    @KafkaListener(topics = "inventory-reserved", groupId = "saga-group")
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received inventory reserved event: {}", event);
        
        OrderSaga saga = sagaRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Saga not found for orderId: " + event.getOrderId()));
        
        if (event.isSuccess()) {
            saga.setReservationId(event.getReservationId());
            updateSagaStatus(saga, SagaStatus.INVENTORY_RESERVED, SagaStep.COMPLETE_ORDER);
            
            // Complete the saga
            completeSaga(saga);
        } else {
            log.error("Inventory reservation failed for orderId: {}", event.getOrderId());
            // Start compensation
            compensate(saga);
        }
    }
    
    private void completeSaga(OrderSaga saga) {
        log.info("Completing saga for orderId: {}", saga.getOrderId());
        updateSagaStatus(saga, SagaStatus.COMPLETED, saga.getCurrentStep());
    }
    
    private void compensate(OrderSaga saga) {
        log.warn("Starting compensation for orderId: {}", saga.getOrderId());
        updateSagaStatus(saga, SagaStatus.COMPENSATING, saga.getCurrentStep());
        
        // Compensate payment if it was processed
        if (saga.getPaymentId() != null) {
            CompensatePaymentEvent event = CompensatePaymentEvent.builder()
                    .orderId(saga.getOrderId())
                    .paymentId(saga.getPaymentId())
                    .build();
            kafkaTemplate.send(COMPENSATE_PAYMENT_TOPIC, saga.getOrderId(), event);
        }
        
        // Compensate inventory if it was reserved
        if (saga.getReservationId() != null) {
            CompensateInventoryEvent event = CompensateInventoryEvent.builder()
                    .orderId(saga.getOrderId())
                    .reservationId(saga.getReservationId())
                    .build();
            kafkaTemplate.send(COMPENSATE_INVENTORY_TOPIC, saga.getOrderId(), event);
        }
        
        updateSagaStatus(saga, SagaStatus.COMPENSATED, saga.getCurrentStep());
    }
    
    private void updateSagaStatus(OrderSaga saga, SagaStatus status, SagaStep step) {
        saga.setStatus(status);
        saga.setCurrentStep(step);
        sagaRepository.save(saga);
        log.info("Saga updated - OrderId: {}, Status: {}, Step: {}", 
                saga.getOrderId(), status, step);
    }
}
