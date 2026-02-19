package com.wallet.transactionservice.application.services

import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import com.wallet.transactionservice.dtos.web.request.CreateTransactionRequest
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID

class TransactionServiceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var transactionService: TransactionService

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Test
    fun `should persist transaction in database`() {
        val request = CreateTransactionRequest(
            accountId = UUID.randomUUID(),
            amount = BigDecimal("11.1100"),
            currency = Currency.USD.name,
            type = TransactionType.CREDIT.name
        )

        val transaction = transactionService.createTransaction(request)

        val stored = transactionRepository.findById(transaction.transactionId)

        assertEquals(TransactionStatus.PENDING, stored?.status)
        assertEquals(request.accountId, stored?.accountId?.value)
        assertEquals(request.amount, stored?.money?.amount)
        assertEquals(TransactionType.CREDIT, stored?.type)
    }
}