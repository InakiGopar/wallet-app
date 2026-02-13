package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
    fun `should register outbox event as pending`() {
        // given
        val aggregateId = UUID.randomUUID()

        val payload = BalanceUpdatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal.valueOf(200),
            delta = BigDecimal.valueOf(100),
            newBalance = BigDecimal.valueOf(300),
            occurredAt = Instant.now()
        )

        // when
        outboxService.registerBalanceUpdatedEvent(
            aggregateId = aggregateId,
            aggregateType = AggregateType.ACCOUNT,
            eventType = EventType.BALANCE_UPDATED,
            payload = payload
        )

        // then
        val pendingEvents = outboxRepository.findPending(limit = 10)

        assertEquals(1, pendingEvents.size)

        val event = pendingEvents.first()

        assertEquals(aggregateId, event.aggregateId)
        assertEquals(AggregateType.ACCOUNT, event.aggregateType)
        assertEquals(EventType.BALANCE_UPDATED, event.type)
        assertEquals(OutboxStatus.PENDING, event.status)
        //TODO improve this assertEquals
        assertEquals(payload.toString(), event.payload)
        assertNotNull(event.eventId)
        assertNotNull(event.occurredAt)
    }

}