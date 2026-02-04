package com.wallet.transactionservice.infrastructure.messaging.publisher

import com.wallet.transactionservice.dtos.event.BalanceUpdatedEvent
import com.wallet.transactionservice.infrastructure.containers.RabbitIntegrationTest
import com.wallet.transactionservice.infrastructure.messaging.config.RabbitConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID.*


class RabbitEventPublisherIT : RabbitIntegrationTest() {

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