package com.wallet.account.infrastructure.inbound.messaging.listener

import com.ninjasquad.springmockk.MockkBean
import com.wallet.account.domian.models.AccountId
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.dtos.event.TransactionCreatedEvent
import com.wallet.account.infrastructure.containers.RabbitIntegrationTest
import com.wallet.account.infrastructure.outbound.messaging.config.RabbitConfig
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import com.wallet.account.application.services.AccountService
import com.wallet.account.domian.models.microTypes.TransactionType
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


class TransactionCreatedListenerIT : RabbitIntegrationTest() {

    @Autowired
    lateinit var eventPublisher: EventPublisher

    @MockkBean(relaxed = true)
    lateinit var accountService: AccountService

    @Autowired
    lateinit var rabbitListenerEndpointRegistry: RabbitListenerEndpointRegistry

    @BeforeEach
    fun startListeners() {
        rabbitListenerEndpointRegistry.listenerContainers.forEach {
            if (!it.isRunning) it.start()
        }
    }

    @Test
    fun `should consume transaction created event and update balance`() {
        // given
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("50.00")

        val event = TransactionCreatedEvent(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            currency = Currency.USD.name,
            type = TransactionType.CREDIT.name,
            occurredAt = Instant.now(),
        )

        // when
        eventPublisher.publish(
            RabbitConfig.TRANSACTION_CREATED_ROUTING_KEY,
            event
        )

        // then
        val transactionIdSlot = slot<TransactionId>()
        val accountIdSlot = slot<AccountId>()
        val currencySlot = slot<Currency>()
        val deltaSlot = slot<BalanceDelta>()
        val transactionTypeSlot = slot<TransactionType>()

        verify(timeout = 5_000) {
            accountService.tryUpdateBalanceAmount(
                capture(transactionIdSlot),
                capture(accountIdSlot),
                capture(currencySlot),
                capture(deltaSlot),
                capture(transactionTypeSlot)
            )
        }

        // mapping assertions
        assertEquals(transactionId, transactionIdSlot.captured.value)
        assertEquals(accountId, accountIdSlot.captured.value)
        assertEquals(Currency.USD.name, currencySlot.captured.name)
        assertEquals(0, deltaSlot.captured.amount.compareTo(amount))
        assertEquals(TransactionType.CREDIT, transactionTypeSlot.captured)
    }

}