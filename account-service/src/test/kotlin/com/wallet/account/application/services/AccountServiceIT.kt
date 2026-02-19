package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.BalanceUpdateResult
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.domian.models.microTypes.TransactionType
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID


class AccountServiceIT : BaseIntegrationTest()  {

    @Autowired
    lateinit var accountService: AccountService
    @Autowired
    lateinit var accountRepository: AccountRepository
    @Autowired
    lateinit var outboxRepository: OutboxRepository



    @Test
    fun `should create account with zero balance`() {
        val account = accountService.createAccount(Currency.ARS)

        val persisted = accountRepository.findById(account.accountId)

        assertNotNull(persisted)
        assertEquals(AccountStatus.ACTIVE, persisted!!.status)
        assertTrue(
            persisted.balance.money.amount.compareTo(BigDecimal.ZERO) == 0
        )

        assertEquals(Currency.ARS, persisted.currency)
    }


    @Test
    fun `should update balance and register outbox event`() {
        // given
        val transactionId = TransactionId(UUID.randomUUID())
        val account = accountService.createAccount(Currency.ARS)
        val currency = Currency.ARS
        val delta = BalanceDelta(BigDecimal("100.00"))
        val transactionType = TransactionType.CREDIT

        // when
        accountService.tryUpdateBalanceAmount(transactionId, account.accountId,currency, delta, transactionType)

        // then - balance updated
        val updatedAccount = accountRepository.findById(account.accountId)!!
        assertTrue(
            updatedAccount.balance.money.amount.compareTo(BigDecimal("100.00")) == 0
        )

        // then - outbox event created
        val events = outboxRepository.findPending(2)
        assertTrue(events.size == 1)

        val event = events.first()
        assertEquals(AggregateType.ACCOUNT, event.aggregateType)
        assertEquals(EventType.BALANCE_UPDATED, event.type)
        assertNotNull(event.payload)
        assertEquals(OutboxStatus.PENDING, event.status)
        assertEquals(account.accountId.value, event.aggregateId)
    }

    @Test
    fun `should reject updating balance if account is not active and create rejected event`() {
        val transactionId = TransactionId(UUID.randomUUID())
        val account = accountService.createAccount(Currency.ARS)
        val currency = Currency.ARS

        accountService.updateStatus(account.accountId, AccountStatus.SUSPENDED)

        val result = accountService.tryUpdateBalanceAmount(
            transactionId,
            account.accountId,
            currency,
            BalanceDelta(BigDecimal("50")),
            TransactionType.CREDIT
        )

        val updatedAccount = accountRepository.findById(account.accountId)!!
        assertTrue(
            updatedAccount.balance.money.amount.compareTo(BigDecimal.ZERO) == 0
        )

        assertTrue(result is BalanceUpdateResult.Rejected)

        val events = outboxRepository.findPending(10)
        assertEquals(1, events.size)

        val event = events.first()
        assertEquals(AggregateType.ACCOUNT, event.aggregateType)
    }


}