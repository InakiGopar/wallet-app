package com.wallet.account.dtos.web.response

import com.wallet.account.domian.models.Account
import java.math.BigDecimal
import java.util.UUID

data class AccountResponse(
    val accountId: UUID,
    val currency: String,
    val status: String,
    val balance: BigDecimal
) {
    companion object {
        fun from(account: Account): AccountResponse =
            AccountResponse(
                accountId = account.accountId.value,
                currency = account.currency.name,
                status = account.status.name,
                balance = account.balance.money.amount
            )
    }
}