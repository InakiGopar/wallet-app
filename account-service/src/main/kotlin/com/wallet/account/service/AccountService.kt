package com.wallet.account.service

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.exceptions.AccountNotFoundException
import com.wallet.account.domian.exceptions.InvalidAccountStateException
import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.utils.serializer.EventSerializer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val outboxService: OutboxService,
    private val eventSerializer: EventSerializer
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
    fun updateBalance(accountId: AccountId, delta: BalanceDelta) {

        val account = getAccount(accountId)

        //check 1
        if (account.status != AccountStatus.ACTIVE) {
            throw InvalidAccountStateException(accountId, account.status)
        }

        val previousBalance = account.balance.money.amount
        val newBalance = Money(previousBalance + delta.amount, account.currency)


        accountRepository.updateBalance(accountId, newBalance)

        //Convert to JSON
        //Payload is the domain event data
        val payload = eventSerializer.serialize(
            BalanceUpdatedEvent(
                accountId = account.accountId.value,
                previousBalance = previousBalance,
                delta = delta.amount,
                newBalance = newBalance.amount,
                occurredAt = Instant.now()
            )
        )
        //Call the outbox service
        outboxService.registerEvent(
            aggregateId = account.accountId.value,
            aggregateType = AggregateType.ACCOUNT,
            eventType = EventType.BALANCE_UPDATED,
            payload = payload
        )
    }


    @Transactional
    fun updateStatus(accountId: AccountId, status: AccountStatus) {
        accountRepository.updateStatus(accountId, status)
    }
}