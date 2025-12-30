package com.wallet.account.infrastructure.web.dtos.response

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
                accountId = account.accountId,
                currency = account.currency,
                status = account.status.name,
                balance = account.balance.amount
            )
    }
}