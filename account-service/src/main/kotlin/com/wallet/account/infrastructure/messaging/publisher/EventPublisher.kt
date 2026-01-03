package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.dtos.event.DomainEvent

interface EventPublisher {
    fun publish(routingKey: String, event: DomainEvent)
}