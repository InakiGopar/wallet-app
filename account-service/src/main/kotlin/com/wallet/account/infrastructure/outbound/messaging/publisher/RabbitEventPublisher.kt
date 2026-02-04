package com.wallet.account.infrastructure.outbound.messaging.publisher

import com.wallet.account.infrastructure.outbound.messaging.config.RabbitConfig
import com.wallet.account.infrastructure.outbound.messaging.exception.EventPublishException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitEventPublisher(
    private val rabbit: RabbitTemplate
) : EventPublisher {

    override fun publish(routingKey: String, payload: Any) {
        try {
            rabbit.convertAndSend(
                RabbitConfig.WALLET_EXCHANGE,
                routingKey,
                payload
            )
        }
        catch (e: Exception) {
            throw EventPublishException(
                message = "Failed to publish event to RabbitMQ [routingKey=$routingKey]",
                cause = e
            )
        }
    }
}