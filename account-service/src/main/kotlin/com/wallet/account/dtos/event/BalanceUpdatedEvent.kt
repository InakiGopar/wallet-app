package com.wallet.account.dtos.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class BalanceUpdatedEvent(
    val accountId: UUID,
    val previousBalance: BigDecimal,
    val delta: BigDecimal,
    val newBalance: BigDecimal,
    override val occurredAt: Instant
) : EventMessage
