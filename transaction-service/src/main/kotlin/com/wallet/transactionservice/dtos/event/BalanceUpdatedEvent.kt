package com.wallet.transactionservice.dtos.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class BalanceUpdatedEvent(
    val transactionId: UUID,
    val accountId: UUID,
    val previousBalance: BigDecimal,
    val delta: BigDecimal,
    val newBalance: BigDecimal,
    val occurredAt: Instant
)
