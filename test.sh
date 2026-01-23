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
