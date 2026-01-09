package com.wallet.account.domian.exceptions

import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.Money

class InsufficientFundsException(
    val accountId: AccountId,
    val currentBalance: Money,
    val attemptedDelta: Money
) : RuntimeException(
    "Insufficient funds for account ${accountId.value}. " +
            "Current balance: ${currentBalance.amount}, " +
            "Attempted delta: ${attemptedDelta.amount}"
)