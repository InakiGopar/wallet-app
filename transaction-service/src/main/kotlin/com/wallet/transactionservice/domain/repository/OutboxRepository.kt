package com.wallet.transactionservice.domain.repository

import com.wallet.transactionservice.domain.events.OutboxEvent
import java.util.UUID

interface OutboxRepository {
    fun save(event: OutboxEvent)
    fun findById(eventId: UUID): OutboxEvent?
    fun findPending(limit: Int): List<OutboxEvent>
    fun findPendingForUpdate(limit: Int): List<OutboxEvent>
    fun markAsSent(eventId: UUID)
    fun markAsFailed(eventId: UUID)
}