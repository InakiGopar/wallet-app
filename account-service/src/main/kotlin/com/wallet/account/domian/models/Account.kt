package com.wallet.account.domian.models

import java.time.Instant
import java.util.UUID

data class Account(
    val accountId: UUID,
    val currency: String,
    val status: AccountStatus,
    val createdAt: Instant,
    val balance: Balance,
)
