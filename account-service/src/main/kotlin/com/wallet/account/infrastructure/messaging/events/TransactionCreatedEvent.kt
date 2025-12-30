package com.wallet.account.infrastructure.messaging.events

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TransactionCreatedEvent(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val createdAt: Instant
)
