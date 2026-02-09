package com.wallet.account.domian.exceptions

import com.wallet.account.domian.models.AccountId
import java.math.BigDecimal

class InsufficientFundsException(
    val accountId: AccountId,
    val attemptedDebit: BigDecimal,
    val currentBalance: BigDecimal
) : RuntimeException(
    "Insufficient funds for account ${accountId.value}. " +
            "Attempted debit: $attemptedDebit, current balance: $currentBalance"
)
