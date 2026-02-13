package com.wallet.account.utils.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import org.springframework.stereotype.Component

@Component
class BalanceUpdatedEventSerializer(
    private val objectMapper: ObjectMapper
) : EventSerializer<BalanceUpdatedEvent> {
    override fun serialize(event: Any)
    : String = objectMapper.writeValueAsString(event)

}