package com.wallet.account.infrastructure.persistence.jooq

import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.repository.OutboxRepository
import org.springframework.stereotype.Repository
import java.util.UUID



@Repository
class JooqOutboxRepository : OutboxRepository {
    override fun save(event: OutboxEvent) {
        TODO("Not yet implemented")
    }

    override fun findPending(limit: Int): List<OutboxEvent> {
        TODO("Not yet implemented")
    }

    override fun markAsSent(eventId: UUID) {
        TODO("Not yet implemented")
    }

    override fun markAsFailed(eventId: UUID) {
        TODO("Not yet implemented")
    }
}