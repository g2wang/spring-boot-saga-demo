package com.example.saga.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Topic names as constants
    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    public static final String PAYMENT_PROCESSED_TOPIC = "payment-processed";
    public static final String INVENTORY_EVENTS_TOPIC = "inventory-events";
    public static final String INVENTORY_RESERVED_TOPIC = "inventory-reserved";
    public static final String COMPENSATE_PAYMENT_TOPIC = "compensate-payment";
    public static final String COMPENSATE_INVENTORY_TOPIC = "compensate-inventory";

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name(PAYMENT_PROCESSED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(INVENTORY_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(INVENTORY_RESERVED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic compensatePaymentTopic() {
        return TopicBuilder.name(COMPENSATE_PAYMENT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic compensateInventoryTopic() {
        return TopicBuilder.name(COMPENSATE_INVENTORY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
