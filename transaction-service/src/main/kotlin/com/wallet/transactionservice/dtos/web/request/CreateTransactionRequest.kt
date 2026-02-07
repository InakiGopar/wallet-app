package com.wallet.transactionservice.dtos.web.request

import java.math.BigDecimal
import java.util.UUID

data class CreateTransactionRequest(
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val type: String,
)