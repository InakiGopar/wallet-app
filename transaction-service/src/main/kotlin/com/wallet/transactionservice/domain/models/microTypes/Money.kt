package com.wallet.transactionservice.domain.models.microTypes

import com.wallet.transactionservice.domain.exceptions.NegativeMoneyException
import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    //DDD verification
    init {
        if (amount < BigDecimal.ZERO) {
            throw NegativeMoneyException(amount)
        }
    }
}
