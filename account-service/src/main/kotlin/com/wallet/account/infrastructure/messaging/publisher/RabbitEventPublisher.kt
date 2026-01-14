package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.infrastructure.messaging.config.RabbitConfig
import com.wallet.account.infrastructure.messaging.exception.EventPublishException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitEventPublisher(
    private val rabbit: RabbitTemplate
) : EventPublisher {

    override fun publish(routingKey: String, payload: String) {
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