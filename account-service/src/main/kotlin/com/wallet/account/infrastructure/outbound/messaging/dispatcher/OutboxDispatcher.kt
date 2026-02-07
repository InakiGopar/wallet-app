package com.wallet.account.infrastructure.outbound.messaging.dispatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import org.slf4j.LoggerFactory
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
        //Polling
        val events = outboxRepository.findPendingForUpdate(limit = 50)

        events.forEach { event ->
            try {
                //Convert payload from String to BalanceUpdatedEvent
                val balanceUpdatedEvent =
                    objectMapper.readValue(
                        event.payload,
                        BalanceUpdatedEvent::class.java
                    )
                //publish the event
                eventPublisher.publish(
                    routingKey = "balance.updated",
                    payload = balanceUpdatedEvent
                )

                //update the status PENDING to SENT
                outboxRepository.markAsSent(event.eventId)

            } catch (ex: EventPublishException) {
                LoggerFactory.getLogger(OutboxDispatcher::class.java)
                    .error("Error publishing outbox event ${event.eventId}", ex)

                //If an error occurs change the status PENDING to FAILED
                outboxRepository.markAsFailed(event.eventId)
            }
        }
    }
}