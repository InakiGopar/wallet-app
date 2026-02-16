package com.wallet.account.domian.events

enum class EventType(val routingKey: String) {
    BALANCE_UPDATED("balance.updated"),
    TRANSACTION_REJECTED("transaction.rejected");
}