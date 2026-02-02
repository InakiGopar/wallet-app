package com.wallet.transactionservice.infrastructure.messaging.publisher

interface EventPublisher {
    fun publish(routingKey: String, payload: Any)
}