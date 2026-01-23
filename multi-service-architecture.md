## Multi-Service Saga Architecture

## Project Structure

```
saga-microservices/
├── saga-common/                    # Shared DTOs and Events
│   ├── src/main/java/com/example/saga/common/
│   │   ├── events/
│   │   │   ├── OrderCreatedEvent.java
│   │   │   ├── PaymentProcessedEvent.java
│   │   │   ├── InventoryReservedEvent.java
│   │   │   ├── CompensatePaymentEvent.java
│   │   │   └── CompensateInventoryEvent.java
│   │   └── dto/
│   │       ├── OrderRequest.java
│   │       └── OrderResponse.java
│   └── build.gradle
│
├── order-service/                  # Port 8080
│   ├── src/main/java/com/example/order/
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── OrderServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle
│
├── saga-orchestrator/              # Port 8081
│   ├── src/main/java/com/example/orchestrator/
│   │   ├── model/
│   │   │   ├── OrderSaga.java
│   │   │   ├── SagaStatus.java
│   │   │   └── SagaStep.java
│   │   ├── repository/
│   │   │   └── OrderSagaRepository.java
│   │   ├── service/
│   │   │   └── SagaOrchestrator.java
│   │   ├── config/
│   │   │   └── KafkaTopicConfig.java
│   │   ├── SagaOrchestratorApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle
│
├── payment-service/                # Port 8082
│   ├── src/main/java/com/example/payment/
│   │   ├── service/
│   │   │   └── PaymentService.java
│   │   ├── PaymentServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle
│
├── inventory-service/              # Port 8083
│   ├── src/main/java/com/example/inventory/
│   │   ├── service/
│   │   │   └── InventoryService.java
│   │   ├── InventoryServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle
│
├── docker-compose.yml              # Run all services
└── settings.gradle                 # Multi-module config
```

## Why Separate Services?

### 1. **Independent Deployment**
- Each service can be deployed, scaled, and updated independently
- No need to redeploy everything for a small change

### 2. **Independent Scaling**
- Scale orchestrator separately based on order volume
- Scale payment service based on transaction load
- Scale inventory service based on product catalog size

### 3. **Technology Independence**
- Payment service could use Java
- Inventory service could use Node.js or Python
- Each team can choose their stack

### 4. **Fault Isolation**
- If payment service crashes, order and inventory still work
- Orchestrator maintains saga state across failures

### 5. **Team Autonomy**
- Different teams can own different services
- Clear boundaries and responsibilities

## Communication Flow

1. **Order Service** receives HTTP request
2. **Order Service** publishes `OrderCreatedEvent` to Kafka
3. **Saga Orchestrator** listens and starts saga workflow
4. **Orchestrator** sends commands to Payment and Inventory services
5. **Services** publish response events back to Kafka
6. **Orchestrator** listens to responses and progresses saga
7. **Orchestrator** triggers compensation if needed

## Key Differences from Current Demo

| Aspect | Demo (Current) | Production |
|--------|---------------|------------|
| Deployment | Single JAR | Multiple JARs/Containers |
| Scaling | All or nothing | Service-specific |
| Database | Shared H2 | Separate databases per service |
| Failure | Entire app down | Only affected service down |
| Team Structure | Single team | Multiple teams |
| Technology | Same stack | Can be polyglot |

## Benefits of Orchestrator as Separate Service

1. **Single Source of Truth**: Orchestrator owns saga state
2. **Centralized Logic**: All workflow logic in one place
3. **Easy Monitoring**: Monitor saga progress in one location
4. **Simpler Compensation**: Orchestrator knows full saga history
5. **Service Independence**: Other services are simpler, only respond to commands
