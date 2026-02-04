package com.wallet.account.infrastructure.outbound.messaging.dispatcher


import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class OutboxDispatcherTest {

    @MockK
    lateinit var outboxRepository: OutboxRepository

    @MockK
    lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    lateinit var dispatcher: OutboxDispatcher


    @Test
    fun `should publish event and mark it as SENT`() {
        // Arrange
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"amount":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )


        every { outboxRepository.findPendingForUpdate(50) } returns listOf(event)
        every { eventPublisher.publish(any(), any()) } just Runs
        every { outboxRepository.markAsSent(event.eventId) } just Runs

        // Act
        dispatcher.dispatch()

        // Assert
        verify(exactly = 1) {
            eventPublisher.publish(
                routingKey = event.type.name,
                payload = event.payload
            )
        }

        verify(exactly = 1) {
            outboxRepository.markAsSent(event.eventId)
        }

        verify(exactly = 0) {
            outboxRepository.markAsFailed(any())
        }
    }


    @Test
    fun `should mark event as FAILED when publishing fails`() {
        // Arrange
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"amount":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        every { outboxRepository.findPendingForUpdate(50) } returns listOf(event)

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("Rabbit down")

        every { outboxRepository.markAsFailed(event.eventId) } just Runs

        // Act
        dispatcher.dispatch()

        // Assert
        verify(exactly = 1) {
            outboxRepository.markAsFailed(event.eventId)
        }

        verify(exactly = 0) {
            outboxRepository.markAsSent(any())
        }
    }

}