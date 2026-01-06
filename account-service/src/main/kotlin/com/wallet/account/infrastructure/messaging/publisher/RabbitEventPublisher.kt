package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.infrastructure.messaging.config.RabbitConfig
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitEventPublisher(
    private val rabbit: RabbitTemplate
) : EventPublisher {

    override fun publish(routingKey: String, payload: String) {
        rabbit.convertAndSend(
            RabbitConfig.WALLET_EXCHANGE,
            routingKey,
            payload
        )
    }
}