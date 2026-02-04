package com.wallet.transactionservice.infrastructure.outbound.messaging.listener

import com.wallet.transactionservice.domain.models.AccountId
import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import com.wallet.transactionservice.dtos.event.BalanceUpdatedEvent
import com.wallet.transactionservice.infrastructure.containers.RabbitIntegrationTest
import com.wallet.transactionservice.infrastructure.outbound.messaging.config.RabbitConfig
import com.wallet.transactionservice.infrastructure.outbound.messaging.publisher.EventPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit


class BalanceUpdatedListenerIT : RabbitIntegrationTest() {

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var rabbitListenerEndpointRegistry: RabbitListenerEndpointRegistry

    @BeforeEach
    fun startRabbitListeners() {
        rabbitListenerEndpointRegistry.listenerContainers.forEach { container ->
            if (!container.isRunning) {
                container.start()
            }
        }
    }

    @Test
    fun `should consume BalanceUpdatedEvent and complete transaction`() {
        val transactionId = TransactionId(UUID.randomUUID())

        val event = BalanceUpdatedEvent(
            transactionId = transactionId.value,
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("50.00"),
            delta = BigDecimal("50.00"),
            newBalance = BigDecimal("150.00"),
            occurredAt = Instant.now()
        )

        val transaction = Transaction(
            transactionId = transactionId,
            accountId = AccountId(event.accountId),
            money = Money(
                amount = BigDecimal("50.00"),
                currency = Currency.USD,
            ),
            status = TransactionStatus.PENDING,
            type = TransactionType.CREDIT,
            createdAt = Instant.now()
        )

        transactionRepository.save(transaction)

        eventPublisher.publish(
            routingKey = RabbitConfig.BALANCE_UPDATED_ROUTING_KEY,
            payload = event
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted {
                val tx = transactionRepository.findById(TransactionId(event.transactionId))
                assertEquals(TransactionStatus.COMPLETED, tx?.status)
            }
    }

}