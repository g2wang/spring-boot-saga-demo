package com.example.saga.repository;

import com.example.saga.model.OrderSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderSagaRepository extends JpaRepository<OrderSaga, Long> {
    Optional<OrderSaga> findByOrderId(String orderId);
}
