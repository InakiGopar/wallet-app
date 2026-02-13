package com.wallet.transactionservice.domain.events

import java.util.UUID

class TransactionRejectedEvent(
    val transactionId: UUID,
    val reason: String)