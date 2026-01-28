package com.wallet.transactionservice.dtos.event

import com.wallet.transactionservice.domain.models.microTypes.Currency
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TransactionCreatedEvent(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    val type: String,
    val createdAt: Instant,
    val occurredAt: Instant
)