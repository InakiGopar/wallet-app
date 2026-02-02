package com.wallet.transactionservice.domain.repository

import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId

interface TransactionRepository {

    fun save(transaction: Transaction)

    fun updateStatus(transaction: Transaction)

    fun findById(id: TransactionId): Transaction?
}