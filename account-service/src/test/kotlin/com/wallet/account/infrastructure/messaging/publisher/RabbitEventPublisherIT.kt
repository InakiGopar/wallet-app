package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.dtos.event.TransactionCreatedEvent
import com.wallet.account.infrastructure.containers.RabbitIntegrationTest
import com.wallet.account.infrastructure.messaging.config.RabbitConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


class RabbitEventPublisherIT : RabbitIntegrationTest() {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var publisher: RabbitEventPublisher

    @Test
    fun `should publish event to transaction created queue`() {
        // given
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("50.00")

        val event = TransactionCreatedEvent(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            currency = Currency.USD,
            type = EventType.BALANCE_UPDATED.name,
            createdAt = Instant.now(),
            occurredAt = Instant.now(),
        )

        // when
        publisher.publish(
            routingKey = RabbitConfig.TRANSACTION_CREATED_ROUTING_KEY,
            payload = event
        )

        // then
        val received = rabbitTemplate.receiveAndConvert(
            RabbitConfig.TRANSACTION_CREATED_QUEUE,
            2000
        )

        assertEquals(event, received)
    }
}