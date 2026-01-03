package com.wallet.account.service

import com.wallet.account.domian.exceptions.AccountNotFoundException
import com.wallet.account.domian.exceptions.InvalidAccountStateException
import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.infrastructure.messaging.publisher.EventPublisher
import com.wallet.account.domian.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val eventPublisher: EventPublisher
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
    fun updateBalance(accountId: AccountId, newAmount: Money) {

        val account = getAccount(accountId)

        //check 1
        if (account.status != AccountStatus.ACTIVE) {
            throw InvalidAccountStateException(accountId, account.status)
        }

        accountRepository.updateBalance(accountId, newAmount)

        val event = BalanceUpdatedEvent(
            accountId = accountId.value,
            newBalance = newAmount.amount,
            occurredAt = Instant.now()
        )

        eventPublisher.publish(
            "balance.update",
            event
        )

    }

    @Transactional
    fun updateStatus(accountId: AccountId, status: AccountStatus) {
        accountRepository.updateStatus(accountId, status)
    }
}