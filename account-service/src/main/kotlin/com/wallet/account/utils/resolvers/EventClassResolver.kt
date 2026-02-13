package com.wallet.account.utils.resolvers

import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.TransactionRejectedEvent
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import org.springframework.stereotype.Component

@Component
class EventClassResolver {
    private val classMap = mapOf(
        EventType.BALANCE_UPDATED to BalanceUpdatedEvent::class.java,
        EventType.TRANSACTION_REJECTED to TransactionRejectedEvent::class.java
    )

    fun resolve(eventType: EventType): Class<*> =
        classMap[eventType]
            ?: throw IllegalArgumentException("No class for $eventType")
}