package com.wallet.account.utils.resolvers

import com.wallet.account.domian.events.EventType
import org.springframework.stereotype.Component

@Component
class EventRoutingResolver {

    private val routingMap = mapOf(
        EventType.BALANCE_UPDATED to "balance.updated",
        EventType.TRANSACTION_REJECTED to "transaction.rejected"
    )

    fun resolve(eventType: EventType): String =
        routingMap[eventType]
            ?: throw IllegalArgumentException("No routing key for $eventType")
}
