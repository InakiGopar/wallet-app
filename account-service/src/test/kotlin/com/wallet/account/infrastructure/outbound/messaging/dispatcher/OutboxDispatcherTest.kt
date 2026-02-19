package com.wallet.account.infrastructure.outbound.messaging.dispatcher


import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import com.wallet.account.utils.resolvers.EventClassResolver
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
    lateinit var objectMapper: ObjectMapper

    @MockK
    lateinit var eventClassResolver: EventClassResolver

    @MockK
    lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    lateinit var dispatcher: OutboxDispatcher


    @Test
    fun `should deserialize publish and mark as SENT`() {

        val eventId = UUID.randomUUID()

        val outboxEvent = OutboxEvent(
            eventId = eventId,
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"accountId":"123","newBalance":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        val fakeEventObject = Any()

        every { outboxRepository.findPendingForUpdate(50) } returns listOf(outboxEvent)
        every { eventClassResolver.resolve(EventType.BALANCE_UPDATED) } returns Any::class.java
        every { objectMapper.readValue(outboxEvent.payload, Any::class.java) } returns fakeEventObject
        every { eventPublisher.publish(any(), any()) } just Runs
        every { outboxRepository.markAsSent(eventId) } just Runs

        dispatcher.dispatch()

        verify {
            eventClassResolver.resolve(EventType.BALANCE_UPDATED)
        }

        verify {
            objectMapper.readValue(outboxEvent.payload, Any::class.java)
        }

        verify {
            eventPublisher.publish(
                EventType.BALANCE_UPDATED.routingKey,
                fakeEventObject
            )
        }

        verify {
            outboxRepository.markAsSent(eventId)
        }

        verify(exactly = 0) {
            outboxRepository.markAsFailed(any())
        }
    }


    @Test
    fun `should mark event as FAILED when publish throws EventPublishException`() {

        val eventId = UUID.randomUUID()

        val outboxEvent = OutboxEvent(
            eventId = eventId,
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        val fakeEventObject = Any()

        every { outboxRepository.findPendingForUpdate(50) } returns listOf(outboxEvent)
        every { eventClassResolver.resolve(any()) } returns Any::class.java
        every { objectMapper.readValue(any<String>(), Any::class.java) } returns fakeEventObject

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("Rabbit down")

        every { outboxRepository.markAsFailed(eventId) } just Runs

        dispatcher.dispatch()

        verify {
            outboxRepository.markAsFailed(eventId)
        }

        verify(exactly = 0) {
            outboxRepository.markAsSent(any())
        }
    }

}