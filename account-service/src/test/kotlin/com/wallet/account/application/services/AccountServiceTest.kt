package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.BalanceUpdateResult
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.exceptions.AccountNotFoundException
import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountId
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.domian.models.microTypes.TransactionType
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


@ExtendWith(MockKExtension::class)
class AccountServiceTest {


    @MockK
    lateinit var accountRepository: AccountRepository
    @MockK
    lateinit var outboxService: OutboxService

    @InjectMockKs
    lateinit var accountService: AccountService



    @Test
    fun `createAccount creates account with zero balance and ACTIVE status`() {
        every { accountRepository.create(any()) } answers { firstArg() }

        val account = accountService.createAccount(Currency.USD)

        assert(account.status == AccountStatus.ACTIVE)
        assert(account.balance.money.amount == BigDecimal.ZERO)
        assert(account.currency == Currency.USD)

        verify { accountRepository.create(any()) }
    }


    @Test
    fun `getAccount throws AccountNotFoundException when account does not exist`() {
        val accountId = AccountId(UUID.randomUUID())

        every { accountRepository.findById(accountId) } returns null

        assertThrows<AccountNotFoundException> {
            accountService.getAccount(accountId)
        }
    }



    @Test
    fun `updateBalance returns Rejected and registers rejected event if account is not ACTIVE`() {
        val transactionId = TransactionId(UUID.randomUUID())
        val acc = account(
            balance = BigDecimal("100"),
            status = AccountStatus.SUSPENDED
        )

        every { accountRepository.findById(acc.accountId) } returns acc
        every { outboxService.registerRejectedEvent(any(), any()) } returns Unit

        val result = accountService.tryUpdateBalanceAmount(
            transactionId,
            acc.accountId,
            acc.currency,
            BalanceDelta(BigDecimal("10")),
            TransactionType.CREDIT
        )

        assert(result is BalanceUpdateResult.Rejected)

        verify(exactly = 0) {
            accountRepository.updateBalanceAmount(any(), any())
        }

        verify {
            outboxService.registerRejectedEvent(
                transactionId,
                any()
            )
        }
    }




    @Test
    fun `updateStatus updates account status`() {
        val accountId = AccountId(UUID.randomUUID())

        every { accountRepository.updateStatus(accountId, AccountStatus.CLOSED) } returns Unit

        accountService.updateStatus(accountId, AccountStatus.CLOSED)

        verify {
            accountRepository.updateStatus(accountId, AccountStatus.CLOSED)
        }
    }



    @Test
    fun `updateBalance updates balance and registers balance updated event`() {
        val transactionId = TransactionId(UUID.randomUUID())
        val acc = account(
            balance = BigDecimal("100"),
            status = AccountStatus.ACTIVE
        )

        every { accountRepository.findById(acc.accountId) } returns acc
        every { accountRepository.updateBalanceAmount(any(), any()) } returns Unit
        every { outboxService.registerBalanceUpdatedEvent(any(), any(), any(), any()) } returns Unit

        val delta = BalanceDelta(BigDecimal("50"))

        val result = accountService.tryUpdateBalanceAmount(
            transactionId,
            acc.accountId,
            acc.currency,
            delta,
            TransactionType.CREDIT
        )

        assert(result is BalanceUpdateResult.Applied)

        verify {
            accountRepository.updateBalanceAmount(
                acc.accountId,
                Money(BigDecimal("150"), acc.currency)
            )
        }

        val payloadSlot = slot<BalanceUpdatedEvent>()

        verify {
            outboxService.registerBalanceUpdatedEvent(
                aggregateId = acc.accountId.value,
                aggregateType = AggregateType.ACCOUNT,
                eventType = EventType.BALANCE_UPDATED,
                payload = capture(payloadSlot)
            )
        }

        val captured = payloadSlot.captured

        assertEquals(transactionId.value, captured.transactionId)
        assertEquals(acc.accountId.value, captured.accountId)
        assertEquals(BigDecimal("100"), captured.previousBalance)
        assertEquals(BigDecimal("50"), captured.delta)
        assertEquals(BigDecimal("150"), captured.newBalance)
    }




    //Helper
    private fun account(
        balance: BigDecimal,
        status: AccountStatus = AccountStatus.ACTIVE,
        currency: Currency = Currency.USD
    ): Account {
        val accountId = AccountId(UUID.randomUUID())
        val now = Instant.now()

        return Account(
            accountId = accountId,
            currency = currency,
            status = status,
            createdAt = now,
            balance = Balance(
                accountId = accountId,
                money = Money(balance, currency),
                updatedAt = now
            )
        )
    }

}