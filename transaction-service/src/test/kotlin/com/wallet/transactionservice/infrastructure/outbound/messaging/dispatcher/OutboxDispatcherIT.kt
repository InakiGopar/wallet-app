package com.wallet.transactionservice.infrastructure.outbound.messaging.dispatcher


import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.infrastructure.outbound.messaging.publisher.EventPublisher
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import com.ninjasquad.springmockk.MockkBean
import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import com.wallet.transactionservice.infrastructure.outbound.messaging.exception.EventPublishException
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OutboxDispatcherIT : BaseIntegrationTest() {

    @Autowired
    lateinit var dispatcher: OutboxDispatcher

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var eventPublisher: EventPublisher



    @Test
    fun `dispatcher should publish event and mark it as sent`() {

        val outboxEvent = newOutboxEvent()
        outboxRepository.save(outboxEvent)

        every {
            eventPublisher.publish(any(), any())
        } returns Unit

        dispatcher.dispatch()

        val updated = outboxRepository.findById(outboxEvent.eventId)

        assertEquals(OutboxStatus.SENT, updated!!.status)

        verify(exactly = 1) {
            eventPublisher.publish(
                outboxEvent.type.routingKey,
                any<TransactionCreatedEvent>()
            )
        }
    }

    @Test
    fun `dispatcher should mark event as failed when publishing fails`() {

        val outboxEvent = newOutboxEvent()
        outboxRepository.save(outboxEvent)

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("boom")

        dispatcher.dispatch()

        val updated = outboxRepository.findById(outboxEvent.eventId)

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
    private fun newOutboxEvent(): OutboxEvent {

        val eventPayload = TransactionCreatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = BigDecimal("222.00"),
            currency = Currency.ARS,
            type = TransactionType.CREDIT.name,
            occurredAt = Instant.now()
        )

        return OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.TRANSACTION,
            type = EventType.TRANSACTION_CREATED,
            payload = objectMapper.writeValueAsString(eventPayload),
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )
    }
}