package com.wallet.transactionservice.infrastructure.persistence

import com.wallet.transactionservice.domain.models.AccountId
import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class TransactionRepositoryIT : BaseIntegrationTest() {
    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Test
    fun `should persist and load transaction`() {
        // given
        val transactionId = TransactionId(UUID.randomUUID())
        val accountId = AccountId(UUID.randomUUID())

        val transaction = Transaction(
            transactionId = transactionId,
            accountId = accountId,
            money = Money(
                amount = BigDecimal("100.00"),
                currency = Currency.USD,
            ),
            type = TransactionType.DEBIT,
            status = TransactionStatus.COMPLETED,
            createdAt = Instant.now()
        )

        // when
        transactionRepository.save(transaction)
        val loaded = transactionRepository.findById(transaction.transactionId)

        // then
        assertNotNull(loaded)
        assertEquals(
            0,
            loaded!!.money.amount.compareTo(BigDecimal("100.00"))
        )

    }
}