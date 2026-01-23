# Option 1: Docker Compose (easiest)
docker-compose up

# Option 2: Manual (4 terminals)
# Terminal 1: Kafka
docker run -p 9092:9092 apache/kafka:3.7.0

# Terminal 2: Order Service
cd order-service && ./gradlew bootRun

# Terminal 3: Saga Orchestrator
cd saga-orchestrator && ./gradlew bootRun

# Terminal 4: Payment Service
cd payment-service && ./gradlew bootRun

# Terminal 5: Inventory Service
cd inventory-service && ./gradlew bootRun
