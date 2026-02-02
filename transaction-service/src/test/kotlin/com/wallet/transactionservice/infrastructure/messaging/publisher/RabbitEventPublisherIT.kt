package com.wallet.transactionservice.infrastructure.messaging.publisher

import com.wallet.transactionservice.dtos.event.BalanceUpdatedEvent
import com.wallet.transactionservice.infrastructure.messaging.config.RabbitConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID.*

@Testcontainers
@SpringBootTest(
    properties = [
        "spring.rabbitmq.listener.simple.auto-startup=false"
    ]
)
class RabbitEventPublisherIT {
    companion object {

        @Container
        val rabbitMQ = RabbitMQContainer("rabbitmq:3.12-management")
            .withReuse(false)

        @JvmStatic
        @DynamicPropertySource
        fun rabbitProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.rabbitmq.host") { rabbitMQ.host }
            registry.add("spring.rabbitmq.port") { rabbitMQ.amqpPort }
        }
    }

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var publisher: RabbitEventPublisher

    @Test
    fun `should publish event to transaction created queue`() {
        // given
        val event = BalanceUpdatedEvent(
            transactionId = randomUUID(),
            accountId = randomUUID(),
            previousBalance = BigDecimal("50.00"),
            delta = BigDecimal("50.00"),
            newBalance = BigDecimal("150.00"),
            occurredAt = Instant.now()
        )


        // when
        publisher.publish(
            routingKey = RabbitConfig.BALANCE_UPDATED_ROUTING_KEY,
            payload = event
        )

        // then
        val received = rabbitTemplate.receiveAndConvert(
            RabbitConfig.BALANCE_UPDATED_QUEUE,
            2000
        )

        assertEquals(event, received)
    }
}