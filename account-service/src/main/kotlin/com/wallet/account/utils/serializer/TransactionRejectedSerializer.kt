package com.wallet.account.utils.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.domian.events.TransactionRejectedEvent
import org.springframework.stereotype.Component

@Component
class TransactionRejectedSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer<TransactionRejectedEvent> {
    override fun serialize(event: Any):
            String = objectMapper.writeValueAsString(event)
}