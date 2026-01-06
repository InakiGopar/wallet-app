package com.wallet.account.utils.serializer

import com.wallet.account.dtos.event.EventMessage

interface EventSerializer {
    fun serialize(event: EventMessage): String
}