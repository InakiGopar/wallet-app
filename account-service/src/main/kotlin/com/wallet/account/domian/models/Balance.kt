package com.wallet.account.domian.models

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Balance(
    val accountId: UUID,
    val amount: BigDecimal,
    val updatedAt: Instant,
)
