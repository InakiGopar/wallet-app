package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.events.RejectionReason
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OutboxServiceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Autowired
    lateinit var outboxService: OutboxService

    @Test
    fun `should register balance updated event as pending`() {

        val aggregateId = UUID.randomUUID()

        val payload = BalanceUpdatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("200"),
            delta = BigDecimal("100"),
            newBalance = BigDecimal("300"),
            occurredAt = Instant.now()
        )

        outboxService.registerBalanceUpdatedEvent(
            aggregateId = aggregateId,
            aggregateType = AggregateType.ACCOUNT,
            eventType = EventType.BALANCE_UPDATED,
            payload = payload
        )

        val pendingEvents = outboxRepository.findPending(limit = 10)

        assertEquals(1, pendingEvents.size)

        val event = pendingEvents.first()

        assertEquals(aggregateId, event.aggregateId)
        assertEquals(AggregateType.ACCOUNT, event.aggregateType)
        assertEquals(EventType.BALANCE_UPDATED, event.type)
        assertEquals(OutboxStatus.PENDING, event.status)

        // verify JSON
        assertTrue(event.payload.contains(payload.transactionId.toString()))
        assertTrue(event.payload.contains(payload.accountId.toString()))
        assertTrue(event.payload.contains("200"))
        assertTrue(event.payload.contains("300"))

        assertNotNull(event.eventId)
        assertNotNull(event.occurredAt)
    }

    @Test
    fun `should register rejected event as pending`() {

        val transactionId = TransactionId(UUID.randomUUID())
        val reason = RejectionReason.INSUFFICIENT_FUNDS

        outboxService.registerRejectedEvent(transactionId, reason)

        val events = outboxRepository.findPending(limit = 10)

        assertEquals(1, events.size)

        val event = events.first()

        assertEquals(transactionId.value, event.aggregateId)
        assertEquals(AggregateType.ACCOUNT, event.aggregateType)
        assertEquals(EventType.TRANSACTION_REJECTED, event.type)
        assertEquals(OutboxStatus.PENDING, event.status)

        // Valid JSON
        assertTrue(event.payload.contains(transactionId.value.toString()))
        assertTrue(event.payload.contains(reason.name))

        assertNotNull(event.eventId)
        assertNotNull(event.occurredAt)
    }

}