package com.wallet.account.domian.models

import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.Money
import java.time.Instant


data class Balance(
    val accountId: AccountId,
    val money: Money,
    val updatedAt: Instant,
)
