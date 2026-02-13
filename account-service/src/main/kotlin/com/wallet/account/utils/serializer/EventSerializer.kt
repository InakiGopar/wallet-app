package com.wallet.account.utils.serializer


interface EventSerializer<T> {
    fun serialize(event: Any): String
}