package com.wallet.account.infrastructure.outbound.messaging.publisher

import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.dtos.event.TransactionCreatedEvent
import com.wallet.account.infrastructure.containers.RabbitIntegrationTest
import com.wallet.account.infrastructure.outbound.messaging.config.RabbitConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


class RabbitEventPublisherIT : RabbitIntegrationTest() {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var publisher: RabbitEventPublisher

    @Autowired
    lateinit var rabbitListenerEndpointRegistry: RabbitListenerEndpointRegistry

    @BeforeEach
    fun stopListeners() {
        rabbitListenerEndpointRegistry.listenerContainers.forEach {
            if (it.isRunning) it.stop()
        }
    }

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
            currency = Currency.USD.name,
            type = EventType.BALANCE_UPDATED.name,
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
        ) as TransactionCreatedEvent

        assertEquals(transactionId, received.transactionId)
        assertEquals(accountId, received.accountId)
        assertEquals(0, received.amount.compareTo(amount))
    }
}