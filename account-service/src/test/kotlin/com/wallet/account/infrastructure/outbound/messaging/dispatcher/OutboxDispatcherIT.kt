package com.wallet.account.infrastructure.outbound.messaging.dispatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OutboxDispatcherIT : BaseIntegrationTest() {

    @MockkBean
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var dispatcher: OutboxDispatcher

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Test
    fun `dispatch should publish pending events and mark them as SENT`() {

        val outboxEvent = newOutboxEvent()
        outboxRepository.save(outboxEvent)

        every { eventPublisher.publish(any(), any()) } returns Unit

        dispatcher.dispatch()

        val persisted = outboxRepository.findById(outboxEvent.eventId)!!

       assertEquals(OutboxStatus.SENT, persisted.status)

        verify(exactly = 1) {
            eventPublisher.publish(
                EventType.BALANCE_UPDATED.routingKey,
                any()
            )
        }
    }


    @Test
    fun `dispatch should mark event as FAILED when publishing fails`() {

        val outboxEvent = newOutboxEvent()
        outboxRepository.save(outboxEvent)

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("Rabbit down")

        dispatcher.dispatch()

        val persisted = outboxRepository.findById(outboxEvent.eventId)!!

        assertEquals(OutboxStatus.FAILED, persisted.status)

        verify(exactly = 1) {
            eventPublisher.publish(
                any(),
                any()
            )
        }
    }

    //helper
    private fun newOutboxEvent() : OutboxEvent {

        val eventPayload = BalanceUpdatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("33.000"),
            delta = BigDecimal("3.000"),
            newBalance = BigDecimal("36.000"),
            occurredAt = Instant.now()
        )

        return OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = objectMapper.writeValueAsString(eventPayload),
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )
    }

}