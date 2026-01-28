package com.wallet.transactionservice.domain.exceptions

import com.wallet.transactionservice.domain.models.TransactionId

class TransactionAlreadyExistException(transactionId: TransactionId)
    : RuntimeException("Transaction with id $transactionId already exists.")