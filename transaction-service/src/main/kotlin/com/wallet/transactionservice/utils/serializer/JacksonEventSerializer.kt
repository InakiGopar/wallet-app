package com.wallet.transactionservice.utils.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import org.springframework.stereotype.Component

@Component
class JacksonEventSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer {
    override fun serialize(event: TransactionCreatedEvent): String {
        return objectMapper.writeValueAsString(event)
    }

}