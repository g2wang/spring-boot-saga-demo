package com.example.saga.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_saga")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSaga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderId;
    
    private String customerId;
    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    
    @Enumerated(EnumType.STRING)
    private SagaStep currentStep;
    
    private String paymentId;
    private String reservationId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

