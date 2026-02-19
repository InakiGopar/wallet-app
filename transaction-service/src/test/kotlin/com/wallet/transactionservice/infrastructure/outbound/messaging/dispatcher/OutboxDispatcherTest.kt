package com.wallet.transactionservice.infrastructure.outbound.messaging.dispatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import com.wallet.transactionservice.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.transactionservice.infrastructure.outbound.messaging.publisher.EventPublisher
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OutboxDispatcherTest {
    @MockK
    lateinit var outboxRepository: OutboxRepository

    @MockK
    lateinit var eventPublisher: EventPublisher

    @MockK
    lateinit var objectMapper: ObjectMapper

    @InjectMockKs
    lateinit var dispatcher: OutboxDispatcher

    @Test
    fun `should deserialize publish and mark as SENT`() {

        val outboxEventId = UUID.randomUUID()

        val outboxEvent = OutboxEvent(
            eventId = outboxEventId,
            aggregateId = UUID.randomUUID(),
            aggregateType = com.wallet.transactionservice.domain.events.AggregateType.TRANSACTION,
            type = com.wallet.transactionservice.domain.events.EventType.TRANSACTION_CREATED,
            payload = """{"transactionId":"123"}""",
            status = com.wallet.transactionservice.domain.events.OutboxStatus.PENDING,
            occurredAt = java.time.Instant.now()
        )

        val fakeEventObject = mockk<TransactionCreatedEvent>()

        every { outboxRepository.findPendingForUpdate(50) } returns listOf(outboxEvent)

        every {
            objectMapper.readValue(
                outboxEvent.payload,
                TransactionCreatedEvent::class.java
            )
        } returns fakeEventObject

        every { eventPublisher.publish(any(), any()) } just Runs
        every { outboxRepository.markAsSent(outboxEventId) } just Runs

        dispatcher.dispatch()

        verify {
            objectMapper.readValue(
                outboxEvent.payload,
                TransactionCreatedEvent::class.java
            )
        }

        verify {
            eventPublisher.publish(
                outboxEvent.type.routingKey,
                fakeEventObject
            )
        }

        verify {
            outboxRepository.markAsSent(outboxEventId)
        }

        verify(exactly = 0) {
            outboxRepository.markAsFailed(any())
        }
    }


    @Test
    fun `should mark event as FAILED when publish throws EventPublishException`() {

        val outboxEventId = UUID.randomUUID()

        val outboxEvent = OutboxEvent(
            eventId = outboxEventId,
            aggregateId = UUID.randomUUID(),
            aggregateType = com.wallet.transactionservice.domain.events.AggregateType.TRANSACTION,
            type = com.wallet.transactionservice.domain.events.EventType.TRANSACTION_CREATED,
            payload = """{}""",
            status = com.wallet.transactionservice.domain.events.OutboxStatus.PENDING,
            occurredAt = java.time.Instant.now()
        )

        val fakeEventObject = mockk<TransactionCreatedEvent>()

        every { outboxRepository.findPendingForUpdate(50) } returns listOf(outboxEvent)

        every {
            objectMapper.readValue(
                outboxEvent.payload,
                TransactionCreatedEvent::class.java
            )
        } returns fakeEventObject

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("Rabbit down")

        every { outboxRepository.markAsFailed(outboxEventId) } just Runs

        dispatcher.dispatch()

        verify {
            outboxRepository.markAsFailed(outboxEventId)
        }

        verify(exactly = 0) {
            outboxRepository.markAsSent(any())
        }
    }
}