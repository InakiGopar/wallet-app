package com.wallet.account.utils.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.dtos.event.EventMessage
import org.springframework.stereotype.Component

@Component
class JacksonEventSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer {
    override fun serialize(event: EventMessage): String {
        return objectMapper.writeValueAsString(event)
    }
}