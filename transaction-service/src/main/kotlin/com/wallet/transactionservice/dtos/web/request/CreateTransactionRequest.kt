package com.wallet.transactionservice.dtos.web.request

import com.wallet.transactionservice.domain.models.microTypes.Currency
import java.math.BigDecimal
import java.util.UUID

data class CreateTransactionRequest(
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    val type: String,
)