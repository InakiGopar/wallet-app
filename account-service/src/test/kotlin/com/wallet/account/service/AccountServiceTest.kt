package com.wallet.account.service

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.exceptions.AccountNotFoundException
import com.wallet.account.domian.exceptions.InvalidAccountStateException
import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.utils.serializer.EventSerializer
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
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
    @MockK lateinit var outboxService: OutboxService
    @MockK lateinit var eventSerializer: EventSerializer

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
    fun `updateBalance throws InvalidAccountStateException if account is not ACTIVE`() {
        val acc = account(
            balance = BigDecimal("100"),
            status = AccountStatus.SUSPENDED
        )

        every { accountRepository.findById(acc.accountId) } returns acc

        assertThrows<InvalidAccountStateException> {
            accountService.updateBalance(
                acc.accountId,
                BalanceDelta(BigDecimal("10"))
            )
        }

        verify(exactly = 0) {
            accountRepository.updateBalance(any(), any())
            outboxService.registerEvent(any(), any(), any(), any())
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
    fun `updateBalance updates balance and publishes event when account is ACTIVE`() {
        val acc = account(
            balance = BigDecimal("100"),
            status = AccountStatus.ACTIVE
        )

        every { accountRepository.findById(acc.accountId) } returns acc
        every { eventSerializer.serialize(any()) } returns "json-payload"
        every { accountRepository.updateBalance(any(), any()) } returns Unit
        every { outboxService.registerEvent(any(), any(), any(), any()) } returns Unit

        accountService.updateBalance(
            acc.accountId,
            BalanceDelta(BigDecimal("50"))
        )

        verify {
            accountRepository.updateBalance(
                acc.accountId,
                Money(BigDecimal("150"), acc.currency)
            )
        }

        verify {
            outboxService.registerEvent(
                aggregateId = acc.accountId.value,
                aggregateType = AggregateType.ACCOUNT,
                eventType = EventType.BALANCE_UPDATED,
                payload = "json-payload"
            )
        }
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