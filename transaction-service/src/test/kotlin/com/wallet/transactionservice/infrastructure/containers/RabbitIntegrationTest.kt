package com.wallet.transactionservice.infrastructure.containers

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(
    properties = [
        "spring.rabbitmq.listener.simple.auto-startup=false"
    ]
)
@EnableRabbit
abstract class RabbitIntegrationTest {
    companion object {
        @Container
        val rabbit = RabbitMQContainer("rabbitmq:3.13-management")

        @JvmStatic
        @DynamicPropertySource
        fun rabbitProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.host") { rabbit.host }
            registry.add("spring.rabbitmq.port") { rabbit.amqpPort }
        }
    }

}