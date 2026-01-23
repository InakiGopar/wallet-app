package com.wallet.account.infrastructure.persistence

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.domian.repository.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AccountRepositoryIT {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("account_db")
            .withUsername("test")
            .withPassword("test")
    }

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

        assertThat(found).isNotNull
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

        assertThat(updated).isNotNull
        assertThat(updated!!.balance.money.amount.compareTo(BigDecimal("200")))
            .isEqualTo(0)
    }
}