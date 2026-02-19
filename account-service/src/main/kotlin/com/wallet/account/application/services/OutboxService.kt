package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.events.RejectionReason
import com.wallet.account.domian.events.TransactionRejectedEvent
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.utils.serializer.EventSerializer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OutboxService(
    private val outboxRepository: OutboxRepository,
    private val balanceUpdatedEventSerializer: EventSerializer<BalanceUpdatedEvent>,
    private val transactionRejectedSerializer: EventSerializer<TransactionRejectedEvent>
) {

    @Transactional
    fun registerBalanceUpdatedEvent(
        aggregateId: UUID,
        aggregateType: AggregateType,
        eventType: EventType,
        payload: BalanceUpdatedEvent //here goes the event data domain
    ) {

        //Convert to JSON
        //Payload is the event data
        val json = balanceUpdatedEventSerializer.serialize(payload)

        val outboxEvent = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            type = eventType,
            payload = json,
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        outboxRepository.save(outboxEvent)
    }

    @Transactional
    fun registerRejectedEvent(
        transactionId: TransactionId,
        reason: RejectionReason
    ) {

        val json = transactionRejectedSerializer.serialize(
            TransactionRejectedEvent(
                transactionId = transactionId.value,
                reason = reason.name
            )
        )

        val outboxEvent = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = transactionId.value,
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.TRANSACTION_REJECTED,
            payload = json,
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        outboxRepository.save(outboxEvent)
    }
}