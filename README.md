# Saga Orchestration Pattern - Spring Boot Demo

A comprehensive demonstration of the **Saga Orchestration Pattern** for managing distributed transactions across microservices using Spring Boot and Apache Kafka.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Saga Flow](#saga-flow)
- [Error Handling & Compensation](#error-handling--compensation)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Technology Stack](#technology-stack)

## 🎯 Overview

This project demonstrates the **Saga Orchestration Pattern**, a distributed transaction management pattern that maintains data consistency across microservices without using distributed transactions (2PC).

### What is Saga Orchestration?

In a saga orchestration pattern, a central orchestrator coordinates the saga workflow by telling each participant what to do and when. If any step fails, the orchestrator initiates compensating transactions to undo the changes made by previous steps.

### Key Features

- ✅ Centralized orchestration logic
- ✅ Event-driven architecture using Kafka
- ✅ Automatic compensation on failures
- ✅ Saga state persistence
- ✅ RESTful API for order management
- ✅ Real-time status tracking

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Saga Orchestrator                       │
│  - Manages saga workflow                                    │
│  - Coordinates microservices                                │
│  - Handles compensation logic                               │
└───────────┬─────────────────────────────────┬───────────────┘
            │                                 │
            ▼                                 ▼
    ┌───────────────┐                ┌──────────────────┐
    │    Payment    │                │    Inventory     │
    │   Service     │                │     Service      │
    │               │                │                  │
    │ - Process     │                │ - Reserve stock  │
    │   payments    │                │ - Release stock  │
    │ - Refund      │                │   (compensate)   │
    └───────────────┘                └──────────────────┘
            │                                 │
            └────────────┬────────────────────┘
                         ▼
                   ┌──────────┐
                   │  Kafka   │
                   │ Message  │
                   │  Broker  │
                   └──────────┘
```

## ✅ Prerequisites

Before running this project, ensure you have the following installed:

- **Java 25** or higher
- **Gradle 9.x**
- **Docker** (for running Kafka)
- **Git**

## 📁 Project Structure

```
saga-orchestration-demo/
├── src/main/java/com/example/saga/
│   ├── controller/
│   │   └── OrderController.java          # REST API endpoints
│   ├── dto/
│   │   ├── OrderRequest.java             # Request DTO
│   │   └── OrderResponse.java            # Response DTO
│   ├── events/
│   │   ├── OrderCreatedEvent.java
│   │   ├── PaymentProcessedEvent.java
│   │   ├── InventoryReservedEvent.java
│   │   ├── CompensatePaymentEvent.java
│   │   └── CompensateInventoryEvent.java
│   ├── model/
│   │   ├── OrderSaga.java                # Saga entity
│   │   ├── SagaStatus.java               # Status enum
│   │   └── SagaStep.java                 # Step enum
│   ├── orchestrator/
│   │   └── SagaOrchestrator.java         # Central orchestrator
│   ├── repository/
│   │   └── OrderSagaRepository.java      # JPA repository
│   ├── service/
│   │   ├── PaymentService.java           # Payment microservice
│   │   └── InventoryService.java         # Inventory microservice
│   └── SagaOrchestratorApplication.java  # Main application
├── src/main/resources/
│   └── application.yml                    # Configuration
├── build.gradle                           # Build configuration
└── README.md
```

## 🚀 Getting Started

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd saga-orchestration-demo
```

### Step 2: Start Kafka

Using Docker:

```bash
docker run -d \
  --name kafka \
  -p 9092:9092 \
  apache/kafka:3.7.0
```

Alternatively, using Docker Compose (create `docker-compose.yml`):

```yaml
version: '3.8'
services:
  kafka:
    image: apache/kafka:3.7.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
```

Then run:

```bash
docker-compose up -d
```

### Step 3: Build the Project

```bash
./gradlew build
```

### Step 4: Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Step 5: Access H2 Console

Navigate to `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:sagadb`
- **Username**: `sa`
- **Password**: (leave empty)

## 📚 API Documentation

### Create Order

**Endpoint:** `POST /api/orders`

**Request Body:**
```json
{
  "customerId": "customer-123",
  "productId": "product-456",
  "quantity": 2,
  "amount": 99.99
}
```

**Response:**
```json
{
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "ORDER_CREATED",
  "message": "Order saga initiated successfully"
}
```

**cURL Example:**
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

### Get Order Status

**Endpoint:** `GET /api/orders/{orderId}`

**Response:**
```json
{
  "id": 1,
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "customer-123",
  "productId": "product-456",
  "quantity": 2,
  "amount": 99.99,
  "status": "COMPLETED",
  "currentStep": "COMPLETE_ORDER",
  "paymentId": "pay-xyz789",
  "reservationId": "res-abc123",
  "createdAt": "2024-01-22T10:30:00",
  "updatedAt": "2024-01-22T10:30:05"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/orders/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Get All Orders

**Endpoint:** `GET /api/orders`

**Response:**
```json
[
  {
    "id": 1,
    "orderId": "order-1",
    "status": "COMPLETED",
    ...
  },
  {
    "id": 2,
    "orderId": "order-2",
    "status": "COMPENSATED",
    ...
  }
]
```

**cURL Example:**
```bash
curl http://localhost:8080/api/orders
```

## 🔄 Saga Flow

### Success Flow

```
1. CREATE_ORDER (PENDING)
   ↓
2. PROCESS_PAYMENT (ORDER_CREATED)
   ↓
3. Payment Success → PAYMENT_PROCESSED
   ↓
4. RESERVE_INVENTORY
   ↓
5. Inventory Success → INVENTORY_RESERVED
   ↓
6. COMPLETE_ORDER → COMPLETED ✅
```

### Failure Flow with Compensation

```
1. CREATE_ORDER (PENDING)
   ↓
2. PROCESS_PAYMENT (ORDER_CREATED)
   ↓
3. Payment Success → PAYMENT_PROCESSED
   ↓
4. RESERVE_INVENTORY
   ↓
5. Inventory Failure ❌
   ↓
6. START COMPENSATION (COMPENSATING)
   ↓
7. Refund Payment
   ↓
8. COMPENSATED ✅
```

## 🛡️ Error Handling & Compensation

### Saga States

- **PENDING**: Initial state
- **ORDER_CREATED**: Order created successfully
- **PAYMENT_PROCESSED**: Payment completed
- **INVENTORY_RESERVED**: Inventory reserved
- **COMPLETED**: All steps successful
- **FAILED**: Saga failed (no compensation needed)
- **COMPENSATING**: Compensation in progress
- **COMPENSATED**: Successfully rolled back

### Compensation Logic

The orchestrator automatically triggers compensation when:

1. **Payment succeeds** but **inventory fails** → Refund payment
2. **Payment fails** → No compensation needed (nothing to rollback)

### Simulated Failure Rates

- **Payment Service**: 10% failure rate
- **Inventory Service**: 20% failure rate

This ensures you'll see both success and compensation scenarios when testing.

## 🧪 Testing

### Manual Testing Script

```bash
#!/bin/bash

echo "Creating 10 test orders..."
for i in {1..10}
do
  echo "Creating order $i"
  RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"customerId\": \"customer-$i\",
      \"productId\": \"product-$((RANDOM % 5 + 1))\",
      \"quantity\": $((RANDOM % 10 + 1)),
      \"amount\": $((RANDOM % 1000 + 100))
    }")
  
  ORDER_ID=$(echo $RESPONSE | jq -r '.orderId')
  echo "Order created: $ORDER_ID"
  
  # Wait a bit before checking status
  sleep 3
  
  # Check final status
  STATUS=$(curl -s http://localhost:8080/api/orders/$ORDER_ID | jq -r '.status')
  echo "Final status: $STATUS"
  echo "---"
done
```

### Unit Testing (Example)

```java
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SagaOrchestratorTest {
    
    @Autowired
    private SagaOrchestrator orchestrator;
    
    @Autowired
    private OrderSagaRepository repository;
    
    @Test
    @Order(1)
    void testCreateOrder() {
        OrderRequest request = OrderRequest.builder()
            .customerId("test-customer")
            .productId("test-product")
            .quantity(5)
            .amount(new BigDecimal("100.00"))
            .build();
        
        OrderSaga saga = orchestrator.startSaga(request);
        
        assertNotNull(saga.getOrderId());
        assertEquals(SagaStatus.ORDER_CREATED, saga.getStatus());
    }
}
```

## 📊 Monitoring

### Check Kafka Topics

```bash
# List topics
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Consume messages from a topic
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

### View Logs

```bash
# Application logs
tail -f logs/spring-boot-application.log

# Or view in real-time during development
./gradlew bootRun --console=plain
```

### Database Queries

Connect to H2 console and run:

```sql
-- View all sagas
SELECT * FROM ORDER_SAGA;

-- View completed sagas
SELECT * FROM ORDER_SAGA WHERE STATUS = 'COMPLETED';

-- View compensated sagas
SELECT * FROM ORDER_SAGA WHERE STATUS = 'COMPENSATED';

-- Count by status
SELECT STATUS, COUNT(*) FROM ORDER_SAGA GROUP BY STATUS;
```

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.2.0 |
| Language | Java | 17 |
| Build Tool | Gradle | 8.x |
| Messaging | Apache Kafka | 3.7.0 |
| Database | H2 (in-memory) | Latest |
| ORM | Spring Data JPA | 3.2.0 |
| Serialization | Spring Kafka JSON | Latest |

## 🎓 Learning Resources

### Key Concepts

- **Saga Pattern**: Managing distributed transactions
- **Orchestration vs Choreography**: Centralized vs distributed coordination
- **Compensation**: Rollback mechanisms in distributed systems
- **Event-Driven Architecture**: Async communication patterns

### Further Reading

- [Microservices Patterns by Chris Richardson](https://microservices.io/patterns/data/saga.html)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Saga Pattern Explained](https://docs.microsoft.com/en-us/azure/architecture/reference-architectures/saga/saga)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the MIT License.

## 📧 Contact

For questions or feedback, please open an issue in the repository.

---

**Happy Coding! 🚀**
