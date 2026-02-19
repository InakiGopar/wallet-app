package com.wallet.transactionservice.infrastructure.outbound.persistence

import com.wallet.transactionservice.domain.models.AccountId
import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import com.wallet.transactionservice.infrastructure.containers.BaseIntegrationTest
import org.junit.jupiter.api.Assertions
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
                amount = BigDecimal("777.00"),
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
        Assertions.assertNotNull(loaded)
        Assertions.assertEquals(
            0,
            loaded!!.money.amount.compareTo(BigDecimal("777.00"))
        )

    }
}