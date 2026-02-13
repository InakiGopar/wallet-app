package com.wallet.account.infrastructure.outbound.messaging.dispatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.outbound.messaging.publisher.EventPublisher
import com.wallet.account.utils.resolvers.EventClassResolver
import com.wallet.account.utils.resolvers.EventRoutingResolver
import org.slf4j.LoggerFactory
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
    private val eventRoutingResolver: EventRoutingResolver
) {


    @Transactional
    @Scheduled(fixedDelay = 1000)
    fun dispatch() {
        //Polling
        val events = outboxRepository.findPendingForUpdate(limit = 50)

        events.forEach { outboxEvent ->
            try {

                // 1️⃣ Convert String to EventType
                val eventType = EventType.valueOf(outboxEvent.type.name)

                // 2️⃣ Resolve class in a dynamic way
                val eventClass = eventClassResolver.resolve(eventType)

                // 3️⃣ deserialize payload
                val event =
                    objectMapper.readValue(outboxEvent.payload, eventClass)

                // 4️⃣ Resolve routingKey in a dynamic way
                val routingKey = eventRoutingResolver.resolve(eventType)



                //publish the event
                eventPublisher.publish(
                    routingKey = routingKey,
                    payload = event
                )

                //update the status PENDING to SENT
                outboxRepository.markAsSent(outboxEvent.eventId)

            } catch (ex: EventPublishException) {
                LoggerFactory.getLogger(OutboxDispatcher::class.java)
                    .error("Error publishing outbox event ${outboxEvent.eventId}", ex)

                //If an error occurs change the status PENDING to FAILED
                outboxRepository.markAsFailed(outboxEvent.eventId)
            }
        }
    }
}