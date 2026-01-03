package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.infrastructure.messaging.config.RabbitConfig
import com.wallet.account.dtos.event.DomainEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitEventPublisher(
    private val rabbit: RabbitTemplate
) : EventPublisher {

    override fun publish(routingKey: String, event: DomainEvent) {
        rabbit.convertAndSend(
            RabbitConfig.WALLET_EXCHANGE,
            routingKey,
            event
        )
    }
}