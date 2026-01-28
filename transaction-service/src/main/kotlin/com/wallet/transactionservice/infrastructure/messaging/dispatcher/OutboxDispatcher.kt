package com.wallet.transactionservice.infrastructure.messaging.dispatcher

import com.wallet.transactionservice.domain.repository.OutboxRepository
import com.wallet.transactionservice.infrastructure.messaging.exception.EventPublishException
import com.wallet.transactionservice.infrastructure.messaging.publisher.EventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxDispatcher(
    private val outboxRepository: OutboxRepository,
    private val eventPublisher: EventPublisher
) {
    @Transactional
    @Scheduled(fixedDelay = 1000)
    fun dispatch() {

        val events = outboxRepository.findPendingForUpdate(50)

        events.forEach { event ->
            try {
                eventPublisher.publish(
                    routingKey = event.type.name,
                    payload = event.payload
                )
                outboxRepository.markAsSent(event.eventId)
            }
            catch (e: EventPublishException) {
                //If an error occurs change the status PENDING to FAILED
                outboxRepository.markAsFailed(event.eventId)
            }
        }
    }
}