package com.wallet.account.infrastructure.messaging.dispatcher

import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.messaging.publisher.EventPublisher
import org.springframework.scheduling.annotation.Scheduled

class OutboxDispatcher(
    private val outboxRepository: OutboxRepository,
    private val eventPublisher: EventPublisher
) {


    @Scheduled(fixedDelay = 1000)
    fun dispatch() {

        //Polling
        val events = outboxRepository.findPending(limit = 50)

        events.forEach { event ->
            try {
                //publish the event
                eventPublisher.publish(
                    routingKey = event.type.name,
                    payload = event.payload
                )

                //update the status PENDING to SENT
                outboxRepository.markAsSent(event.id)

            } catch (ex: Exception) {
                //If an error occurs change the status PENDING to FAILED
                outboxRepository.markAsFailed(event.id)
            }
        }
    }
}