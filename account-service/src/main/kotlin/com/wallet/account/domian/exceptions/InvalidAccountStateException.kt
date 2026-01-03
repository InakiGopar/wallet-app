package com.wallet.account.domian.exceptions

import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus

class InvalidAccountStateException(
    val accountId: AccountId,
    val currentState: AccountStatus
) : RuntimeException( "Account $accountId is in invalid state: $currentState")