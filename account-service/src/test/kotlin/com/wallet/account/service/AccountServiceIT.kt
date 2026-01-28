package com.wallet.account.service

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.exceptions.InvalidAccountStateException
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.domian.repository.OutboxRepository
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AccountServiceIT {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("account_db")
            .withUsername("test")
            .withPassword("test")
    }

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
        val transactionId = UUID.randomUUID()
        val account = accountService.createAccount(Currency.ARS)
        val delta = BalanceDelta(BigDecimal("100.00"))

        // when
        accountService.updateBalance(transactionId, account.accountId, delta)

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
    fun `should fail updating balance if account is not active`() {
        // given
        val account = accountService.createAccount(Currency.ARS)
        accountService.updateStatus(account.accountId, AccountStatus.SUSPENDED)

        // when / then
        assertThrows<InvalidAccountStateException> {
            accountService.updateBalance(
                        UUID.randomUUID(),
                account.accountId,
                BalanceDelta(BigDecimal("50"))
            )
        }

        // no event should be created
        assertTrue(outboxRepository.findPending(2).isEmpty())
    }


}