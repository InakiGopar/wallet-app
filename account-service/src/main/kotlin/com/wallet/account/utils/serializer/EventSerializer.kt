package com.wallet.account.utils.serializer

interface EventSerializer<T> {
    fun serialize(event: T): String
}