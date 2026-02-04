package com.wallet.account.infrastructure.outbound.messaging.publisher


interface EventPublisher {
    fun publish(routingKey: String, payload: Any)
}