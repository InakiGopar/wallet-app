package com.wallet.account.infrastructure.outbound.persistence

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountId
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.domian.repository.AccountRepository
import com.wallet.account.infrastructure.containers.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class AccountRepositoryIT : BaseIntegrationTest() {

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Test
    fun `create and findById work`() {
        val accountId = AccountId(UUID.randomUUID())
        val now = Instant.now()

        val account = Account(
            accountId = accountId,
            currency = Currency.USD,
            status = AccountStatus.ACTIVE,
            createdAt = now,
            balance = Balance(
                accountId = accountId,
                money = Money(
                    amount = BigDecimal.valueOf(100),
                    currency = Currency.USD
                ),
                updatedAt = now
            )
        )

        accountRepository.create(account)

        val found = accountRepository.findById(account.accountId)

        Assertions.assertThat(found).isNotNull
    }

    @Test
    fun `updateBalance changes balance`() {
        val accountId = AccountId(UUID.randomUUID())
        val now = Instant.now()

        val account = Account(
            accountId = accountId,
            currency = Currency.USD,
            status = AccountStatus.ACTIVE,
            createdAt = now,
            balance = Balance(
                accountId = accountId,
                money = Money(
                    amount = BigDecimal.valueOf(100),
                    currency = Currency.USD
                ),
                updatedAt = now
            )
        )
        accountRepository.create(account)

        val newMoney = Money(
            amount = BigDecimal.valueOf(200),
            currency = Currency.USD
        )

        accountRepository.updateBalance(accountId, newMoney)

        val updated = accountRepository.findById(accountId)

        Assertions.assertThat(updated).isNotNull
        Assertions.assertThat(updated!!.balance.money.amount.compareTo(BigDecimal("200")))
            .isEqualTo(0)
    }
}