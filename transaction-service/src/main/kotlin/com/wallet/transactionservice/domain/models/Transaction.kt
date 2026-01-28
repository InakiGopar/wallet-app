package com.wallet.transactionservice.domain.models

import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import java.time.Instant
import java.util.UUID

data class Transaction(
    val transactionId: TransactionId,
    val accountId: AccountId,
    val amount: Money,
    val type: TransactionType,
    val status: TransactionStatus,
    val createdAt: Instant
)

@JvmInline
value class TransactionId(val value: UUID)

@JvmInline
value class AccountId(val value: UUID)
