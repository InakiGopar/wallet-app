package com.wallet.transactionservice.infrastructure.outbound.messaging.publisher

interface EventPublisher {
    fun publish(routingKey: String, payload: Any)
}