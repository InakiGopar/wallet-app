package com.wallet.transactionservice.infrastructure.outbound.messaging.dispatcher


import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.infrastructure.outbound.messaging.publisher.EventPublisher
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import com.ninjasquad.springmockk.MockkBean
import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.infrastructure.outbound.messaging.exception.EventPublishException
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class OutboxDispatcherIT : BaseIntegrationTest() {
    @Autowired
    lateinit var dispatcher: OutboxDispatcher

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @MockkBean
    lateinit var eventPublisher: EventPublisher



    @Test
    fun `dispatcher should publish event and mark it as sent`() {
        val event = newOutboxEvent()
        outboxRepository.save(event)

        every {
            eventPublisher.publish(event.type.name, event.payload)
        } returns Unit

        dispatcher.dispatch()

        val updated = outboxRepository.findById(event.eventId)

        assertEquals(OutboxStatus.SENT, updated!!.status)

        verify(exactly = 1) {
            eventPublisher.publish(event.type.name, event.payload)
        }
    }

    @Test
    fun `dispatcher should mark event as failed when publishing fails`() {
        val event = newOutboxEvent()

        outboxRepository.save(event)

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("boom")

        dispatcher.dispatch()

        val updated = outboxRepository.findById(event.eventId)

        assertEquals(OutboxStatus.FAILED, updated!!.status)

        verify(exactly = 1) {
            eventPublisher.publish(any(), any())
        }
    }


    @Test
    fun `dispatcher should process multiple pending events`() {
        val event1 = newOutboxEvent()
        val event2 = newOutboxEvent()

        outboxRepository.save(event1)
        outboxRepository.save(event2)

        every {
            eventPublisher.publish(any(), any())
        } returns Unit

        dispatcher.dispatch()

        val updated1 = outboxRepository.findById(event1.eventId)
        val updated2 = outboxRepository.findById(event2.eventId)

        assertEquals(OutboxStatus.SENT, updated1!!.status)
        assertEquals(OutboxStatus.SENT, updated2!!.status)

        verify(exactly = 2) {
            eventPublisher.publish(any(), any())
        }
    }




    //Helper
    private fun newOutboxEvent() = OutboxEvent(
        eventId = UUID.randomUUID(),
        aggregateId = UUID.randomUUID(),
        aggregateType = AggregateType.TRANSACTION,
        type = EventType.TRANSACTION_CREATED,
        payload = """{"amount":"100.00"}""",
        status = OutboxStatus.PENDING,
        occurredAt = Instant.now()
    )
}