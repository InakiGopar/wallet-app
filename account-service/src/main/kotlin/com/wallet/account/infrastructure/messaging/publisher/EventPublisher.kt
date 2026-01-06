package com.wallet.account.infrastructure.messaging.publisher


interface EventPublisher {
    fun publish(routingKey: String, payload: String)
}