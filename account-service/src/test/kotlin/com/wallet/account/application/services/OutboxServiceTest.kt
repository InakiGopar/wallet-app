package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OutboxServiceTest {

    @MockK
    lateinit var outboxRepository: OutboxRepository

    @InjectMockKs
    lateinit var outboxService: OutboxService



    @Test
    fun `should register outbox event with pending status`() {
        // given
        val aggregateId = UUID.randomUUID()
        val aggregateType = AggregateType.ACCOUNT
        val eventType = EventType.BALANCE_UPDATED
        val payload = BalanceUpdatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal.valueOf(200),
            delta = BigDecimal.valueOf(100),
            newBalance = BigDecimal.valueOf(300),
            occurredAt = Instant.now()
        )

        val slot = slot<OutboxEvent>()

        every { outboxRepository.save(any()) } just Runs

        // when
        outboxService.registerBalanceUpdatedEvent(
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            eventType = eventType,
            payload = payload
        )

        // then
        verify(exactly = 1) {
            outboxRepository.save(capture(slot))
        }

        val savedEvent = slot.captured

        assertNotNull(savedEvent.eventId)
        assertEquals(aggregateId, savedEvent.aggregateId)
        assertEquals(aggregateType, savedEvent.aggregateType)
        assertEquals(eventType, savedEvent.type)
        //TODO improve this assertEquals
        assertEquals(payload.toString(), savedEvent.payload)
        assertEquals(OutboxStatus.PENDING, savedEvent.status)
        assertNotNull(savedEvent.occurredAt)
    }


}