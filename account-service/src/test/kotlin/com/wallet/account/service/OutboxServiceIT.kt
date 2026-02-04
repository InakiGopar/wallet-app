package com.wallet.account.service

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
        val payload = """{"accountId":"$aggregateId","newBalance":100}"""

        // when
        outboxService.registerEvent(
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
        assertEquals(payload, event.payload)
        assertNotNull(event.eventId)
        assertNotNull(event.occurredAt)
    }

}