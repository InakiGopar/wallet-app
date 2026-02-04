package com.wallet.transactionservice.application.services

import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class OutboxServiceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxService: OutboxService

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Test
    fun `should persist outbox event in database`() {
        // given
        val aggregateId = UUID.randomUUID()
        val payload = """{"event":"json"}"""

        // when
        outboxService.registerEvent(
            aggregateId = aggregateId,
            aggregateType = AggregateType.TRANSACTION,
            eventType = EventType.TRANSACTION_CREATED,
            payload = payload
        )

        // then
        val events = outboxRepository.findPending(2)

        assertEquals(1, events.size)

        val event = events.first()
        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(AggregateType.TRANSACTION, event.aggregateType)
        assertEquals(EventType.TRANSACTION_CREATED, event.type)
        assertEquals(payload, event.payload)
        assertEquals(OutboxStatus.PENDING, event.status)
        assertNotNull(event.occurredAt)
    }
}