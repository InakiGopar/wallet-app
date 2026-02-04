package com.wallet.account.infrastructure.outbound.persistence

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions
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

        Assertions.assertNotNull(loaded)
        Assertions.assertEquals(event.eventId, loaded!!.eventId)
        Assertions.assertEquals(event.status, loaded.status)
        Assertions.assertEquals(event.type, loaded.type)
        Assertions.assertEquals(event.aggregateType, loaded.aggregateType)
        Assertions.assertEquals(event.payload, loaded.payload)
    }

    @Test
    fun `should return only pending events`() {

        val sent = newOutboxEvent(OutboxStatus.SENT)

        outboxRepository.save(sent)

        val pendingEvents = outboxRepository.findPending(limit = 10)

        //Expected 4 because in the db are four outboxEvent saved
        //Yes is a strange issue, this may have happened because I ran this test without @Transaction
        Assertions.assertEquals(0, pendingEvents.size)
        Assertions.assertTrue(pendingEvents.all { it.status == OutboxStatus.PENDING })
    }

    @Test
    fun `should mark event as sent`() {
        val event = newOutboxEvent(OutboxStatus.PENDING)
        outboxRepository.save(event)

        outboxRepository.markAsSent(event.eventId)

        val updated = outboxRepository.findById(event.eventId)

        Assertions.assertNotNull(updated)
        Assertions.assertEquals(OutboxStatus.SENT, updated!!.status)
    }

    @Test
    fun `should mark event as failed`() {
        val event = newOutboxEvent(OutboxStatus.PENDING)
        outboxRepository.save(event)

        outboxRepository.markAsFailed(event.eventId)

        val updated = outboxRepository.findById(event.eventId)

        Assertions.assertNotNull(updated)
        Assertions.assertEquals(OutboxStatus.FAILED, updated!!.status)
    }

    //helper
    private fun newOutboxEvent(
        status: OutboxStatus = OutboxStatus.PENDING,
    ) = OutboxEvent(
        eventId = UUID.randomUUID(),
        aggregateId = UUID.randomUUID(),
        aggregateType = AggregateType.ACCOUNT,
        type = EventType.BALANCE_UPDATED,
        payload = """{"accountId":"123","newBalance":100}""",
        status = status,
        occurredAt = Instant.now()
    )
}