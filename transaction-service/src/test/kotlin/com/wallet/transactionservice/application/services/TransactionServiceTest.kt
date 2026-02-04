package com.wallet.transactionservice.application.services

import com.wallet.transactionservice.domain.events.AggregateType
import com.wallet.transactionservice.domain.events.EventType
import com.wallet.transactionservice.domain.exceptions.TransactionAlreadyExistException
import com.wallet.transactionservice.domain.exceptions.TransactionIsAlreadyCompleteException
import com.wallet.transactionservice.domain.exceptions.TransactionNotFoundException
import com.wallet.transactionservice.domain.models.AccountId
import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import com.wallet.transactionservice.dtos.event.BalanceUpdatedEvent
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import com.wallet.transactionservice.dtos.web.request.CreateTransactionRequest
import com.wallet.transactionservice.utils.serializer.EventSerializer
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TransactionServiceTest {

    @MockK
    lateinit var transactionRepository: TransactionRepository

    @MockK
    lateinit var eventSerializer: EventSerializer<TransactionCreatedEvent>

    @MockK
    lateinit var outboxService: OutboxService

    @InjectMockKs
    lateinit var transactionService: TransactionService


    @Test
    fun `should create transaction and register outbox event`() {
        // given
        val request = CreateTransactionRequest(
            accountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.ARS,
            type = "CREDIT"
        )

        every { transactionRepository.findById(any()) } returns null
        every { transactionRepository.save(any()) } just Runs
        every { eventSerializer.serialize(any()) } returns """{"event":"json"}"""
        every { outboxService.registerEvent(any(), any(), any(), any()) } just Runs

        // when
        val result = transactionService.createTransaction(request)

        // then
        assertEquals(TransactionStatus.PENDING, result.status)

        verify(exactly = 1) {
            outboxService.registerEvent(
                any(),
                AggregateType.TRANSACTION,
                EventType.TRANSACTION_CREATED,
                any()
            )
        }
    }


    @Test
    fun `should throw exception if transaction already exists`() {
        // given
        val request = CreateTransactionRequest(
            accountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.USD,
            type = "CREDIT"
        )

        every { transactionRepository.findById(any()) } returns mockk()

        // when / then
        assertThrows<TransactionAlreadyExistException> {
            transactionService.createTransaction(request)
        }

        verify(exactly = 0) {
            transactionRepository.save(any())
            outboxService.registerEvent(any(), any(), any(), any())
        }
    }


    @Test
    fun `should mark transaction as completed`() {
        // given
        val txId = TransactionId(UUID.randomUUID())

        val event = BalanceUpdatedEvent(
            transactionId = txId.value,
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("50.00"),
            delta = BigDecimal("50.00"),
            newBalance = BigDecimal("100.00"),
            occurredAt = Instant.now()
        )

        val transaction = Transaction(
            transactionId = txId,
            accountId = AccountId(event.accountId),
            money = Money(BigDecimal("50.00"), Currency.USD),
            status = TransactionStatus.PENDING,
            type = TransactionType.CREDIT,
            createdAt = Instant.now()
        )

        every { transactionRepository.findById(txId) } returns transaction
        every { transactionRepository.updateStatus(any()) } just Runs

        // when
        transactionService.markAsCompleted(event)

        // then
        verify {
            transactionRepository.updateStatus(
                match { it.status == TransactionStatus.COMPLETED }
            )
        }
    }

    @Test
    fun `should throw exception if transaction not found`() {
        // given
        val event = BalanceUpdatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("0"),
            delta = BigDecimal("0"),
            newBalance = BigDecimal("0"),
            occurredAt = Instant.now()
        )

        every {
            transactionRepository.findById(TransactionId(event.transactionId))
        } returns null

        // when / then
        assertThrows<TransactionNotFoundException> {
            transactionService.markAsCompleted(event)
        }

        verify(exactly = 0) {
            transactionRepository.updateStatus(any())
        }
    }

    @Test
    fun `should throw exception if transaction already completed`() {
        // given
        val txId = TransactionId(UUID.randomUUID())

        val event = BalanceUpdatedEvent(
            transactionId = txId.value,
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("0"),
            delta = BigDecimal("0"),
            newBalance = BigDecimal("0"),
            occurredAt = Instant.now()
        )

        val completedTransaction = Transaction(
            transactionId = txId,
            accountId = AccountId(event.accountId),
            money = Money(BigDecimal("50"), Currency.USD),
            status = TransactionStatus.COMPLETED,
            type = TransactionType.CREDIT,
            createdAt = Instant.now()
        )

        every { transactionRepository.findById(txId) } returns completedTransaction

        // when / then
        assertThrows<TransactionIsAlreadyCompleteException> {
            transactionService.markAsCompleted(event)
        }

        verify(exactly = 0) {
            transactionRepository.updateStatus(any())
        }
    }
}