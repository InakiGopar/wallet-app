package com.wallet.account.infrastructure.outbound.messaging.dispatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import com.wallet.account.utils.resolvers.EventClassResolver
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxDispatcher(
    private val outboxRepository: OutboxRepository,
    private val eventPublisher: EventPublisher,
    //serialization helpers
    private val objectMapper: ObjectMapper,
    private val eventClassResolver: EventClassResolver,
) {


    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun dispatch() {
        //Polling
        val events = outboxRepository.findPendingForUpdate(limit = 50)

        events.forEach { outboxEvent ->
            try {

                val eventType = outboxEvent.type

                // Resolve class in a dynamic way
                val eventClass = eventClassResolver.resolve(eventType)

                // Deserialize payload
                val event =
                    objectMapper.readValue(outboxEvent.payload, eventClass)


                //publish the event
                eventPublisher.publish(
                    routingKey = eventType.routingKey,
                    payload = event
                )

                //update the status PENDING to SENT
                outboxRepository.markAsSent(outboxEvent.eventId)

            } catch (e: EventPublishException) {
                //If an error occurs change the status PENDING to FAILED
                outboxRepository.markAsFailed(outboxEvent.eventId)
            }
        }
    }
}