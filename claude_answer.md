## Please create spring boot project (with gradle and application.yml) demonstrating the Saga Orchestration of microservices

I'll create a comprehensive Spring Boot project demonstrating the Saga Orchestration pattern for microservices. This will include an Order service, Payment service, Inventory service, and an Orchestrator to manage the saga flow.I've created a comprehensive Spring Boot project demonstrating the **Saga Orchestration Pattern** for microservices. Here's what's included:

## Project Structure

### Configuration Files
- **build.gradle**: Dependencies for Spring Boot, Kafka, JPA, H2, and Lombok
- **application.yml**: Configuration for database, Kafka, and application settings

### Domain Models
- **OrderSaga**: Entity tracking the saga state
- **SagaStatus & SagaStep**: Enums for saga lifecycle
- **DTOs**: OrderRequest and OrderResponse

### Event Models
- **OrderCreatedEvent**: Initial order creation
- **PaymentProcessedEvent**: Payment service response
- **InventoryReservedEvent**: Inventory service response
- **Compensation Events**: For rollback operations

### Core Components

**SagaOrchestrator**: Central coordinator that:
- Initiates the saga workflow
- Listens to events from services
- Coordinates the next steps
- Handles compensation (rollback) on failures

**PaymentService**: Simulates payment processing with 90% success rate

**InventoryService**: Simulates inventory reservation with 80% success rate

**OrderController**: REST API endpoints

## How to Run

1. **Start Kafka** (requires Docker):
```bash
docker run -p 9092:9092 apache/kafka:3.7.0
```

2. **Run the application**:
```bash
./gradlew bootRun
```

3. **Create an order**:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "productId": "product-456",
    "quantity": 2,
    "amount": 99.99
  }'
```

4. **Check order status**:
```bash
curl http://localhost:8080/api/orders/{orderId}
```

## Saga Flow

1. **Create Order** → Order created in PENDING state
2. **Process Payment** → Payment service processes payment
3. **Reserve Inventory** → Inventory service reserves stock
4. **Complete** → Saga marked as COMPLETED

If any step fails, compensation events are triggered to rollback previous successful steps (refund payment, release inventory).

The orchestrator maintains the saga state in H2 database and coordinates all microservices through Kafka events, ensuring eventual consistency across the distributed system!
