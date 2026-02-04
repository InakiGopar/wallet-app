package com.wallet.account.infrastructure.outbound.messaging.dispatcher

import com.ninjasquad.springmockk.MockkBean
import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.UUID

class OutboxDispatcherIT : BaseIntegrationTest() {

    @MockkBean
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var dispatcher: OutboxDispatcher

    @Autowired
    lateinit var outboxRepository: OutboxRepository


    @Test
    fun `dispatch should publish pending events and mark them as SENT`() {
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"accountId":"123","newBalance":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        outboxRepository.save(event)

        every { eventPublisher.publish(any(), any()) } returns Unit

        dispatcher.dispatch()

        val persisted = outboxRepository.findById(event.eventId)!!

        Assertions.assertThat(persisted.status).isEqualTo(OutboxStatus.SENT)

        verify {
            eventPublisher.publish("BALANCE_UPDATED", any())
        }
    }


    @Test
    fun `dispatch should mark event as FAILED when publishing fails`() {
        // Arrange
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"accountId":"123","newBalance":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        outboxRepository.save(event)

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("Rabbit down")

        // Act
        dispatcher.dispatch()

        // Assert
        val persisted = outboxRepository.findById(event.eventId)
        Assertions.assertThat(persisted).isNotNull
        Assertions.assertThat(persisted!!.status).isEqualTo(OutboxStatus.FAILED)
    }

}