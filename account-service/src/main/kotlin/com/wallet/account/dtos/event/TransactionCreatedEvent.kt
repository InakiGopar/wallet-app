package com.wallet.account.dtos.event

import com.wallet.account.domian.models.microTypes.Currency
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TransactionCreatedEvent(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    val createdAt: Instant,
    override val occurredAt: Instant
) : DomainEvent
