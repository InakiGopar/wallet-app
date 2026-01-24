package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.infrastructure.messaging.config.RabbitConfig
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

@Testcontainers
@SpringBootTest(
    properties = [
        "spring.rabbitmq.listener.simple.auto-startup=false"
    ]
)
class RabbitEventPublisherIT  {

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
        val payload = """{ "type": "TRANSACTION_CREATED" }"""

        // when
        publisher.publish(
            routingKey = RabbitConfig.TRANSACTION_CREATED_ROUTING_KEY,
            payload = payload
        )

        // then
        val received = rabbitTemplate.receiveAndConvert(
            RabbitConfig.TRANSACTION_CREATED_QUEUE,
            2000
        )

        assertEquals(payload, received)
    }
}