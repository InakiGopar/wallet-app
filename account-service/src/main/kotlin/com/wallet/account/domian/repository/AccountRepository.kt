package com.wallet.account.domian.repository

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.Money


interface AccountRepository {
    fun create(account: Account): Account
    fun findById(accountId: AccountId): Account?
    fun updateBalance(accountId: AccountId, newAmount: Money)
    fun updateStatus(accountId: AccountId, status: AccountStatus)
}