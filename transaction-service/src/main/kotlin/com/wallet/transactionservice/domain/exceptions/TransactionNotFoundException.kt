package com.wallet.transactionservice.domain.exceptions

import com.wallet.transactionservice.domain.models.TransactionId

class TransactionNotFoundException(transactionId: TransactionId)
    : RuntimeException("Transaction with id $transactionId not found")