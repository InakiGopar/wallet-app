package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OutboxService(
    private val outboxRepository: OutboxRepository,
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