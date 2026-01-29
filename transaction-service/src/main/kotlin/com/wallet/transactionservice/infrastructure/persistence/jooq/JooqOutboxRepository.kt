package com.wallet.transactionservice.infrastructure.persistence.jooq

import com.wallet.transaction.jooq.tables.references.OUTBOX_EVENT
import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.events.OutboxEvent
import com.wallet.transactionservice.domain.events.OutboxStatus
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.infrastructure.persistence.jooq.utils.toInstantUtc
import com.wallet.transactionservice.infrastructure.persistence.jooq.utils.toLocalDateTimeUtc
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JooqOutboxRepository(
    private val dsl: DSLContext
) : OutboxRepository {

    override fun save(event: OutboxEvent) {
        dsl.insertInto(OUTBOX_EVENT)
            .set(OUTBOX_EVENT.ID, event.eventId)
            .set(OUTBOX_EVENT.AGGREGATE_ID, event.aggregateId)
            .set(OUTBOX_EVENT.AGGREGATE_TYPE, event.aggregateType.name)
            .set(OUTBOX_EVENT.TYPE, event.type.name)
            .set(OUTBOX_EVENT.PAYLOAD, event.payload)
            .set(OUTBOX_EVENT.STATUS, event.status.name)
            .set(OUTBOX_EVENT.OCCURRED_AT, event.occurredAt.toLocalDateTimeUtc())
            .execute()
    }

    override fun findById(eventId: UUID): OutboxEvent? {
        return dsl.selectFrom(OUTBOX_EVENT)
            .where(OUTBOX_EVENT.ID.eq(eventId))
            .fetchOne {
                    r -> OutboxEvent(
                eventId = r.id!!,
                aggregateId = r.aggregateId!!,
                aggregateType = AggregateType.valueOf(r.aggregateType!!),
                type = EventType.valueOf(r.type!!),
                payload = r.payload!!,
                status = OutboxStatus.valueOf(r.status!!),
                occurredAt = r.occurredAt!!.toInstantUtc(),
            )
            }
    }

    /**
     * Passive read of pending outbox events.
     *
     * Intended for:
     * - tests
     * - debugging / admin queries
     * - observability / metrics
     *
     * ⚠️ This method does NOT apply row locking and MUST NOT be used
     * by the production dispatcher in a multi-instance environment.
     */
    override fun findPending(limit: Int): List<OutboxEvent> {
        return dsl.selectFrom(OUTBOX_EVENT)
            .where(OUTBOX_EVENT.STATUS.eq(OutboxStatus.PENDING.name))
            .orderBy(OUTBOX_EVENT.OCCURRED_AT.asc())
            .limit(limit)
            .fetch { r ->
                OutboxEvent(
                    eventId = r.id!!,
                    aggregateId = r.aggregateId!!,
                    aggregateType = AggregateType.valueOf(r.aggregateType!!),
                    type = EventType.valueOf(r.type!!),
                    payload = r.payload!!,
                    status = OutboxStatus.valueOf(r.status!!),
                    occurredAt = r.occurredAt!!.toInstantUtc(),
                )
            }
    }

    /**
     * Retrieves pending outbox events applying row-level locking
     * (FOR UPDATE SKIP LOCKED).
     *
     * Intended for:
     * - production outbox dispatcher
     * - concurrent / multi-instance environments
     *
     * Guarantees that each event is consumed by a single dispatcher
     * instance, preventing duplicate publications.
     */
    override fun findPendingForUpdate(limit: Int): List<OutboxEvent> {
        return dsl
            .selectFrom(OUTBOX_EVENT)
            .where(OUTBOX_EVENT.STATUS.eq(OutboxStatus.PENDING.name))
            .orderBy(OUTBOX_EVENT.OCCURRED_AT.asc())
            .limit(limit)
            .forUpdate()
            .skipLocked()
            .fetch { r ->
                OutboxEvent(
                    eventId = r.id!!,
                    aggregateId = r.aggregateId!!,
                    aggregateType = AggregateType.valueOf(r.aggregateType!!),
                    type = EventType.valueOf(r.type!!),
                    payload = r.payload!!,
                    status = OutboxStatus.valueOf(r.status!!),
                    occurredAt = r.occurredAt!!.toInstantUtc(),
                )
            }
    }

    override fun markAsSent(eventId: UUID) {
        dsl.update(OUTBOX_EVENT)
            .set(OUTBOX_EVENT.STATUS, OutboxStatus.SENT.name)
            .where(OUTBOX_EVENT.ID.eq(eventId))
            .execute()
    }

    override fun markAsFailed(eventId: UUID) {
        dsl.update(OUTBOX_EVENT)
            .set(OUTBOX_EVENT.STATUS, OutboxStatus.FAILED.name)
            .where(OUTBOX_EVENT.ID.eq(eventId))
            .execute()
    }
}