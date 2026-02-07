package com.wallet.transactionservice.infrastructure.outbound.messaging.dispatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import com.wallet.transactionservice.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.transactionservice.infrastructure.outbound.messaging.publisher.EventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxDispatcher(
    private val outboxRepository: OutboxRepository,
    private val eventPublisher: EventPublisher,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    @Scheduled(fixedDelay = 1000)
    fun dispatch() {
        val events = outboxRepository.findPendingForUpdate(50)

        events.forEach { event ->
            try {
                val transactionCreatedEvent =
                    objectMapper.readValue(
                        event.payload,
                        TransactionCreatedEvent::class.java
                    )
                eventPublisher.publish(
                    routingKey = "transaction.created",
                    payload = transactionCreatedEvent
                )
                outboxRepository.markAsSent(event.eventId)
            }
            catch (e: EventPublishException) {
                //If an error occurs change the status PENDING to FAILED
                outboxRepository.markAsFailed(event.eventId)
                throw EventPublishException("Error publishing event ${event.eventId}", e)
            }
        }
    }
}