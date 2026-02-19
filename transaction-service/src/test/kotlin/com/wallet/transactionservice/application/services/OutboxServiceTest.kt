package com.wallet.transactionservice.application.services

import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import com.wallet.transactionservice.utils.serializer.EventSerializer
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

    @MockK
    lateinit var eventSerializer: EventSerializer<TransactionCreatedEvent>

    @InjectMockKs
    lateinit var outboxService: OutboxService

    @Test
    fun `should register outbox event with PENDING status`() {

        val aggregateId = UUID.randomUUID()

        val payload = TransactionCreatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = BigDecimal.valueOf(100),
            currency = Currency.ARS,
            type = TransactionType.CREDIT.name,
            createdAt = Instant.now(),
            occurredAt = Instant.now()
        )

        val expectedJson = """{"mocked":"json"}"""

        every { eventSerializer.serialize(payload) } returns expectedJson
        every { outboxRepository.save(any()) } just Runs

        outboxService.registerTransactionCreatedEvent(
            aggregateId = aggregateId,
            aggregateType = AggregateType.TRANSACTION,
            eventType = EventType.TRANSACTION_CREATED,
            payload = payload
        )

        val slot = slot<OutboxEvent>()
        verify(exactly = 1) { outboxRepository.save(capture(slot)) }

        val savedEvent = slot.captured

        assertNotNull(savedEvent.eventId)
        assertEquals(aggregateId, savedEvent.aggregateId)
        assertEquals(AggregateType.TRANSACTION, savedEvent.aggregateType)
        assertEquals(EventType.TRANSACTION_CREATED, savedEvent.type)
        assertEquals(expectedJson, savedEvent.payload)
        assertEquals(OutboxStatus.PENDING, savedEvent.status)
        assertNotNull(savedEvent.occurredAt)
    }
}