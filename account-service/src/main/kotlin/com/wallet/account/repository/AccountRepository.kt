package com.wallet.account.repository

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountStatus
import java.math.BigDecimal
import java.util.UUID

interface AccountRepository {
    fun create(account: Account): Account
    fun findById(accountId: UUID): Account?
    fun updateBalance(accountId: UUID, newAmount: BigDecimal)
    fun updateStatus(accountId: UUID, status: AccountStatus)
}