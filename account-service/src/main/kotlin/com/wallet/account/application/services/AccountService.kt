package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.BalanceUpdateResult
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.RejectionReason
import com.wallet.account.domian.exceptions.AccountNotFoundException
import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.domian.models.microTypes.TransactionType
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val outboxService: OutboxService,
) {
    @Transactional
    fun createAccount(currency: Currency): Account {

        val accountId = AccountId(UUID.randomUUID())
        val now = Instant.now()

        val account = Account(
            accountId = accountId,
            currency = currency,
            status = AccountStatus.ACTIVE,
            createdAt = now,
            balance = Balance(
                accountId = accountId,
                money = Money(
                    amount = BigDecimal.ZERO,
                    currency = currency
                ),
                updatedAt = now
            )
        )

        return accountRepository.create(account)
    }


    fun getAccount(accountId: AccountId): Account {
        return accountRepository.findById(accountId)
            ?: throw AccountNotFoundException(accountId)
    }


    @Transactional
    fun tryUpdateBalanceAmount(
        transactionId : TransactionId,
        accountId: AccountId,
        currency: Currency,
        delta: BalanceDelta,
        type: TransactionType
    ) : BalanceUpdateResult {

        val account = accountRepository.findById(accountId)

        // check 1
        if (account == null) {
            outboxService.registerRejectedEvent(transactionId, RejectionReason.ACCOUNT_NOT_FOUND)
            return BalanceUpdateResult.Rejected(RejectionReason.ACCOUNT_NOT_FOUND)
        }

        // check 2
        if (account.status != AccountStatus.ACTIVE) {
            //register the rejected event
            outboxService.registerRejectedEvent(transactionId, RejectionReason.ACCOUNT_NOT_ACTIVE)
            return BalanceUpdateResult.Rejected(
                RejectionReason.ACCOUNT_NOT_ACTIVE
            )
        }

        // check 3
        if (account.currency != currency) {
            outboxService.registerRejectedEvent(transactionId, RejectionReason.CURRENCY_DOESNT_MATCH)
            return BalanceUpdateResult.Rejected(
                RejectionReason.CURRENCY_DOESNT_MATCH
            )
        }

        val previousBalance = account.balance.money.amount

        val newAmount = when (type) {
            TransactionType.CREDIT -> previousBalance + delta.amount
            TransactionType.DEBIT  -> previousBalance - delta.amount
        }

        //check 4
        if (newAmount < BigDecimal.ZERO) {
            //register the rejected event
            outboxService.registerRejectedEvent(transactionId, RejectionReason.INSUFFICIENT_FUNDS)
            return BalanceUpdateResult.Rejected(
                RejectionReason.INSUFFICIENT_FUNDS
            )
        }

        val newBalance = Money(newAmount, account.currency)

        accountRepository.updateBalanceAmount(accountId, newBalance)


        //Call the outbox service
        outboxService.registerBalanceUpdatedEvent(
            aggregateId = account.accountId.value,
            aggregateType = AggregateType.ACCOUNT,
            eventType = EventType.BALANCE_UPDATED,
            //event
            payload = BalanceUpdatedEvent(
                transactionId = transactionId.value,
                accountId = account.accountId.value,
                previousBalance = previousBalance,
                delta = delta.amount,
                newBalance = newBalance.amount,
                occurredAt = Instant.now()
            )
        )

        return BalanceUpdateResult.Applied
    }


    @Transactional
    fun updateStatus(accountId: AccountId, status: AccountStatus) {
        accountRepository.updateStatus(accountId, status)
    }
}