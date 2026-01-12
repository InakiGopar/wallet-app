package com.wallet.account.domian.exceptions

import java.math.BigDecimal

class NegativeMoneyException(amount: BigDecimal) : RuntimeException(
    "Money amount cannot be negative: $amount"
)