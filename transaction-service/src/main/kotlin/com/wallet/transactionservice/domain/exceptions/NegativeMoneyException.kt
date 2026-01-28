package com.wallet.transactionservice.domain.exceptions

import java.math.BigDecimal

class NegativeMoneyException(amount : BigDecimal) : RuntimeException(
    "Money amount cannot be negative: $amount"
)