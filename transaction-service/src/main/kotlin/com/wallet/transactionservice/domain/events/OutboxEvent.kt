package com.wallet.transactionservice.domain.events

import java.time.Instant
import java.util.UUID

data class OutboxEvent(
    val eventId: UUID,
    val aggregateId: UUID,
    val aggregateType: AggregateType,
    val type: EventType,
    val payload: String,
    val status: OutboxStatus,
    val occurredAt: Instant
)
