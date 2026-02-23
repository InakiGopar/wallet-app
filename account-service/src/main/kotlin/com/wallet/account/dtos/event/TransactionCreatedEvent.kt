package com.wallet.account.dtos.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TransactionCreatedEvent(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val type: String,
    override val occurredAt: Instant
) : EventMessage
