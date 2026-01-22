package com.example.saga.controller;

import com.example.saga.dto.OrderRequest;
import com.example.saga.dto.OrderResponse;
import com.example.saga.model.OrderSaga;
import com.example.saga.orchestrator.SagaOrchestrator;
import com.example.saga.repository.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final SagaOrchestrator sagaOrchestrator;
    private final OrderSagaRepository sagaRepository;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        log.info("Received order request: {}", request);
        
        try {
            OrderSaga saga = sagaOrchestrator.startSaga(request);
            
            OrderResponse response = OrderResponse.builder()
                    .orderId(saga.getOrderId())
                    .status(saga.getStatus())
                    .message("Order saga initiated successfully")
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(OrderResponse.builder()
                            .message("Failed to create order: " + e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderSaga> getOrderStatus(@PathVariable String orderId) {
        log.info("Fetching order status for orderId: {}", orderId);
        
        return sagaRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<OrderSaga>> getAllOrders() {
        log.info("Fetching all orders");
        return ResponseEntity.ok(sagaRepository.findAll());
    }
}
