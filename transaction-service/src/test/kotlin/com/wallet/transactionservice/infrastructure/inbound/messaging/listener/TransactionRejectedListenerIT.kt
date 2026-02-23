package com.wallet.transactionservice.infrastructure.inbound.messaging.listener

import com.ninjasquad.springmockk.MockkBean
import com.wallet.transactionservice.domain.events.TransactionRejectedEvent
import com.wallet.transactionservice.domain.models.AccountId
import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import com.wallet.transactionservice.infrastructure.containers.RabbitIntegrationTest
import com.wallet.transactionservice.infrastructure.outbound.messaging.config.RabbitConfig
import com.wallet.transactionservice.infrastructure.outbound.messaging.dispatcher.OutboxDispatcher
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

class TransactionRejectedListenerIT : RabbitIntegrationTest() {

    //Postgres container
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("transaction_db")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun registerDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var registry: RabbitListenerEndpointRegistry

    @MockkBean
    lateinit var outboxDispatcher: OutboxDispatcher

    @BeforeEach
    fun startRabbitListeners() {
        registry.listenerContainers.forEach { container ->
            if (!container.isRunning) {
                container.start()
            }
        }
    }

    @AfterEach
    fun stopRabbitListeners() {
        registry.listenerContainers.forEach { container ->
            container.stop()
        }
    }

    @Test
    fun `should consume TransactionRejectedEvent and mark transaction as failed`() {

        // Arrange
        val transactionId = TransactionId(UUID.randomUUID())
        val accountId = UUID.randomUUID()

        val transaction = Transaction(
            transactionId = transactionId,
            accountId = AccountId(accountId),
            money = Money(
                amount = BigDecimal("55.00"),
                currency = Currency.USD,
            ),
            status = TransactionStatus.PENDING,
            type = TransactionType.DEBIT,
            createdAt = Instant.now()
        )

        transactionRepository.save(transaction)

        val event = TransactionRejectedEvent(
            transactionId = transactionId.value,
            reason = "INSUFFICIENT_FUNDS"
        )

        // Act
        rabbitTemplate.convertAndSend(
            RabbitConfig.WALLET_EXCHANGE,
            RabbitConfig.TRANSACTION_REJECTED_ROUTING_KEY,
            event
        )

        // Assert
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted {
                val updated = transactionRepository.findById(transactionId)
                assertEquals(TransactionStatus.FAILED, updated?.status)
            }
    }
}