package com.wallet.account.service

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.infrastructure.messaging.events.BalanceUpdatedEvent
import com.wallet.account.infrastructure.messaging.publisher.EventPublisher
import com.wallet.account.repository.AccountRepository
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
    fun createAccount(currency: String): Account {
        val accountId = UUID.randomUUID()
        val now = Instant.now()

        val account = Account(
            accountId = accountId,
            currency = currency,
            status = AccountStatus.ACTIVE,
            createdAt = now,
            balance = Balance(
                accountId = accountId,
                amount = BigDecimal.ZERO,
                updatedAt = now
            )
        )

        return accountRepository.create(account)
    }


    fun getAccount(accountId: UUID): Account {
        return accountRepository.findById(accountId)
            ?: throw IllegalArgumentException("Account with id $accountId not found")
    }


    @Transactional
    fun updateBalance(accountId: UUID, newAmount: BigDecimal) {

        //check 1
        require(newAmount > BigDecimal.ZERO) { "Balance cannot be negative" }

        val account = getAccount(accountId)
        //check 2
        if (account.status != AccountStatus.ACTIVE) {
            throw IllegalArgumentException("Cannot update balance of inactive account")
        }

        accountRepository.updateBalance(accountId, newAmount)

        val event = BalanceUpdatedEvent(
            accountId = accountId,
            newBalance = newAmount,
            occurredAt = Instant.now()
        )

        eventPublisher.publish(
            "balance.update",
            event
        )

    }


    @Transactional
    fun updateStatus(accountId: UUID, status: String) {
        val newStatus = AccountStatus.valueOf(status) // convert to AccountStatus
        accountRepository.updateStatus(accountId, newStatus)
    }
}