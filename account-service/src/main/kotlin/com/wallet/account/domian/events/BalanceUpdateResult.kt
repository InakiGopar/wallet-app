package com.wallet.account.domian.events

sealed class BalanceUpdateResult {
    object Applied : BalanceUpdateResult()
    data class Rejected(val reason: RejectionReason) : BalanceUpdateResult()
}
enum class RejectionReason {
    ACCOUNT_NOT_FOUND,
    INSUFFICIENT_FUNDS,
    ACCOUNT_NOT_ACTIVE,
    CURRENCY_DOESNT_MATCH,
}
