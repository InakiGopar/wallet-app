package com.wallet.transactionservice.domain.exceptions

import com.wallet.transactionservice.domain.models.TransactionId

class TransactionIsAlreadyCompleteException(transactionId: TransactionId)
    : RuntimeException("Transaction with id $transactionId already complete")