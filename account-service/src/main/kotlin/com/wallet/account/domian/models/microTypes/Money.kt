package com.wallet.account.domian.models.microTypes

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }
}
