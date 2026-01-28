package com.wallet.transactionservice.dtos.web.response

import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.microTypes.Currency
import java.math.BigDecimal
import java.util.UUID

data class CreateTransactionResponse(
    val transactionId: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    val type: String,
) {
    //mapper
    companion object {
        //mapper
        fun from(transaction: Transaction): CreateTransactionResponse =
            CreateTransactionResponse(
                transactionId = transaction.transactionId.value,
                accountId = transaction.accountId.value,
                amount = transaction.amount.amount,
                currency = transaction.amount.currency,
                type = transaction.type.name,
            )
    }
}
