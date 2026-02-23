package com.wallet.transactionservice.application.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OutboxServiceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var outboxService: OutboxService

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should persist outbox event in database`() {


        val aggregateId = UUID.randomUUID()

        val payload = TransactionCreatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = BigDecimal.valueOf(100),
            currency = Currency.ARS,
            type = TransactionType.CREDIT.name,
            occurredAt = Instant.now()
        )

        outboxService.registerTransactionCreatedEvent(
            aggregateId = aggregateId,
            aggregateType = AggregateType.TRANSACTION,
            eventType = EventType.TRANSACTION_CREATED,
            payload = payload
        )

        val events = outboxRepository.findPending(2)

        assertEquals(1, events.size)

        val event = events.first()

        assertNotNull(event.eventId)
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(AggregateType.TRANSACTION, event.aggregateType)
        assertEquals(EventType.TRANSACTION_CREATED, event.type)
        assertEquals(OutboxStatus.PENDING, event.status)
        assertNotNull(event.occurredAt)

        // Deserializer
        val storedPayload =
            objectMapper.readValue(event.payload, TransactionCreatedEvent::class.java)

        assertEquals(payload.transactionId, storedPayload.transactionId)
        assertEquals(payload.accountId, storedPayload.accountId)
        assertEquals(payload.amount, storedPayload.amount)
        assertEquals(payload.currency, storedPayload.currency)
        assertEquals(payload.type, storedPayload.type)
    }
}