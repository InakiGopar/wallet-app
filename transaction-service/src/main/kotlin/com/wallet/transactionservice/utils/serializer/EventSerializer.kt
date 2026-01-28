package com.wallet.transactionservice.utils.serializer

import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent

interface EventSerializer {
    fun serialize(event: TransactionCreatedEvent): String
}