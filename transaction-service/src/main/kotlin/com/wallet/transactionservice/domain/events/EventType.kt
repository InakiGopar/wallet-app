package com.wallet.transactionservice.domain.events

enum class EventType(val routingKey: String) {
    TRANSACTION_CREATED("transaction.created"),
}