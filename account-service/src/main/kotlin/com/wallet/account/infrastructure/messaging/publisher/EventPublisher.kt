package com.wallet.account.infrastructure.messaging.publisher

import com.wallet.account.infrastructure.messaging.events.DomainEvent

interface EventPublisher {
    fun publish(routingKey: String, event: DomainEvent)
}