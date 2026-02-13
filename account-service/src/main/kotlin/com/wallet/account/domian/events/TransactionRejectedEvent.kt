package com.wallet.account.domian.events

import java.util.UUID

data class TransactionRejectedEvent(
    val transactionId: UUID,
    val reason: String
)
