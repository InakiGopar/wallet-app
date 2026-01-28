package com.wallet.transactionservice.domain.events

enum class OutboxStatus {
    PENDING,
    SENT,
    FAILED
}