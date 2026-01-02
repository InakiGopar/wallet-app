package com.wallet.account.domian.models

import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.Currency
import java.time.Instant

data class Account(
    val accountId: AccountId,
    val currency: Currency,
    val status: AccountStatus,
    val createdAt: Instant,
    val balance: Balance,
)
