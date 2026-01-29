package com.wallet.transactionservice.service

import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.repository.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OutboxService(
    private val outboxRepository : OutboxRepository
) {

    @Transactional
    fun registerEvent(
        aggregateId: UUID,
        aggregateType: AggregateType,
        eventType: EventType,
        payload: String //here goes the event data domain
    ) {
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            type = eventType,
            payload = payload,
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        outboxRepository.save(event)
    }
}