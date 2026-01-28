package com.wallet.account.domian.exceptions

import com.wallet.account.domian.models.AccountId

class AccountNotFoundException(accountId: AccountId)
    : RuntimeException("Account with id $accountId not found")