package com.wallet.account.domian.repository

import com.wallet.account.domian.events.OutboxEvent
import java.util.UUID


interface OutboxRepository {
    fun save(event: OutboxEvent)
    fun findById(eventId: UUID): OutboxEvent?
    fun findPending(limit: Int): List<OutboxEvent>
    fun findPendingForUpdate(limit: Int): List<OutboxEvent>
    fun markAsSent(eventId: UUID)
    fun markAsFailed(eventId: UUID)
}