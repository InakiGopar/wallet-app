package com.wallet.account.infrastructure.messaging.listener

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@SpringBootTest
class TransactionCreatedListenerIT {
    companion object {

        @Container
        val rabbitMQ = RabbitMQContainer("rabbitmq:3.12-management")

        @JvmStatic
        @DynamicPropertySource
        fun rabbitProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.host") { rabbitMQ.host }
            registry.add("spring.rabbitmq.port") { rabbitMQ.amqpPort }
        }
    }
}