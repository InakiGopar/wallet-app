package com.wallet.account.domian.events

import java.time.Instant
import java.util.UUID


data class OutboxEvent(
    val id: UUID,
    val aggregateId: UUID,
    val aggregateType: AggregateType,
    val type: EventType,
    val payload: String,
    val status: OutboxStatus,
    val occurredAt: Instant
)