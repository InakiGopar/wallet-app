package com.wallet.account.domian.models.microTypes

import com.wallet.account.domian.exceptions.NegativeMoneyException
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
