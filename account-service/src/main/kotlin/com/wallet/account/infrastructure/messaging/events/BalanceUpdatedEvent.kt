package com.wallet.account.infrastructure.messaging.events

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class BalanceUpdatedEvent(
    val accountId: UUID,
    val newBalance: BigDecimal,
    val occurredAt: Instant
)
