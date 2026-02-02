package com.wallet.transactionservice.utils.serializer


interface EventSerializer<T> {
    fun serialize(event: T): String
}