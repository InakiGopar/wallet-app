package com.wallet.transactionservice.utils.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import org.springframework.stereotype.Component

@Component
class TransactionCreatedEventSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer<TransactionCreatedEvent> {

    override fun serialize(event: TransactionCreatedEvent): String =
        objectMapper.writeValueAsString(event)
}