package com.wallet.transactionservice.infrastructure.persistence

import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.UUID


class OutboxRepositoryIT : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxRepository: OutboxRepository


    @Test
    fun `should persist and load outbox event`() {
        val event = newOutboxEvent()

        outboxRepository.save(event)

        val loaded = outboxRepository.findById(event.eventId)

        assertNotNull(loaded)
        assertEquals(event.eventId, loaded!!.eventId)
        assertEquals(event.status, loaded.status)
        assertEquals(event.type, loaded.type)
        assertEquals(event.aggregateType, loaded.aggregateType)
        assertEquals(event.payload, loaded.payload)
    }

    @Test
    fun `should return only pending events`() {
        val pending1 = newOutboxEvent(OutboxStatus.PENDING)
        val pending2 = newOutboxEvent(OutboxStatus.PENDING)
        val sent = newOutboxEvent(OutboxStatus.SENT)

        outboxRepository.save(pending1)
        outboxRepository.save(pending2)
        outboxRepository.save(sent)

        val pendingEvents = outboxRepository.findPending(limit = 10)

        assertEquals(2, pendingEvents.size)
        assertTrue(pendingEvents.all { it.status == OutboxStatus.PENDING })
    }

    @Test
    fun `should mark event as sent`() {
        val event = newOutboxEvent(OutboxStatus.PENDING)
        outboxRepository.save(event)

        outboxRepository.markAsSent(event.eventId)

        val updated = outboxRepository.findById(event.eventId)

        assertNotNull(updated)
        assertEquals(OutboxStatus.SENT, updated!!.status)
    }

    @Test
    fun `should mark event as failed`() {
        val event = newOutboxEvent(OutboxStatus.PENDING)
        outboxRepository.save(event)

        outboxRepository.markAsFailed(event.eventId)

        val updated = outboxRepository.findById(event.eventId)

        assertNotNull(updated)
        assertEquals(OutboxStatus.FAILED, updated!!.status)
    }


    //Helper
    private fun newOutboxEvent(
        status: OutboxStatus = OutboxStatus.PENDING
    ) = OutboxEvent(
        eventId = UUID.randomUUID(),
        aggregateId = UUID.randomUUID(),
        aggregateType = AggregateType.TRANSACTION,
        type = EventType.TRANSACTION_CREATED,
        payload = """{"foo":"bar"}""",
        status = status,
        occurredAt = Instant.now()
    )
}