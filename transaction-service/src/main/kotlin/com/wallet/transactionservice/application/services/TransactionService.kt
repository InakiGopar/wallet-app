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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository : TransactionRepository,
    private val outboxService: OutboxService
) {

    @Transactional
    fun createTransaction(request: CreateTransactionRequest) : Transaction {

        val transactionId = TransactionId(UUID.randomUUID())

        //check 1
        if (transactionRepository.findById(transactionId) != null) {
            throw TransactionAlreadyExistException(TransactionId(transactionId.value))
        }

        val transaction = Transaction(
            transactionId = transactionId,
            accountId = AccountId(request.accountId),
            money = Money(request.amount, Currency.valueOf(request.currency)),
            type = TransactionType.valueOf(request.type),
            status = TransactionStatus.PENDING,
            createdAt = Instant.now()
        )

        transactionRepository.save(transaction)

        outboxService.registerTransactionCreatedEvent(
            aggregateId = transaction.transactionId.value,
            aggregateType = AggregateType.TRANSACTION,
            eventType = EventType.TRANSACTION_CREATED,
            //event
            payload = TransactionCreatedEvent(
                transactionId = transactionId.value,
                accountId = transaction.accountId.value,
                amount = transaction.money.amount,
                currency = transaction.money.currency,
                type = transaction.type.name,
                createdAt = transaction.createdAt,
                occurredAt = Instant.now()
            )
        )

        return transaction
    }

    @Transactional
    fun markAsCompleted(event: BalanceUpdatedEvent) {

        //check 1
        val transaction = transactionRepository.findById(TransactionId(event.transactionId))
            ?: throw TransactionNotFoundException(TransactionId(event.transactionId))

        //check 2
        if (transaction.status == TransactionStatus.COMPLETED) {
            throw TransactionIsAlreadyCompleteException(TransactionId(event.transactionId))
        }

        val updated = transaction.copy(status = TransactionStatus.COMPLETED)

        transactionRepository.updateStatus(updated)

    }

    @Transactional
    fun markAsFailed(transactionId: UUID) {
        val transaction = transactionRepository.findById(TransactionId(transactionId))
            ?: throw TransactionNotFoundException(TransactionId(transactionId))

        val updated = transaction.copy(status = TransactionStatus.FAILED)

        transactionRepository.updateStatus(updated)

    }

}