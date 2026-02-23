package com.wallet.account.infrastructure.outbound.persistence.jooq

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountId
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.jooq.tables.references.ACCOUNTS
import com.wallet.account.jooq.tables.references.BALANCES
import com.wallet.account.domian.repository.AccountRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset


@Repository
class JooqAccountRepository(
    private val dsl: DSLContext
): AccountRepository {

    override fun create(account: Account): Account {
        dsl.insertInto(ACCOUNTS)
            .set(ACCOUNTS.ACCOUNT_ID, account.accountId.value)
            .set(ACCOUNTS.CURRENCY, account.currency.name)
            .set(ACCOUNTS.STATUS, account.status.name)
            .set(ACCOUNTS.CREATED_AT, account.createdAt.atOffset(ZoneOffset.UTC))
            .execute()

        dsl.insertInto(BALANCES)
            .set(BALANCES.ACCOUNT_ID, account.accountId.value)
            .set(BALANCES.AMOUNT, account.balance.money.amount)
            .set(BALANCES.UPDATED_AT, account.balance.updatedAt.atOffset(ZoneOffset.UTC))
            .execute()

        return account
    }

    override fun findById(accountId: AccountId): Account? {
        return dsl
            .select()
            .from(ACCOUNTS)
            .join(BALANCES)
            .on(BALANCES.ACCOUNT_ID.eq(ACCOUNTS.ACCOUNT_ID))
            .where(ACCOUNTS.ACCOUNT_ID.eq(accountId.value))
            .fetchOne { r ->
                Account(
                    accountId = AccountId(r.get(ACCOUNTS.ACCOUNT_ID)!!),
                    currency = Currency.valueOf(r.get(ACCOUNTS.CURRENCY)!!),
                    status = AccountStatus.valueOf(r.get(ACCOUNTS.STATUS)!!),
                    createdAt = r.get(ACCOUNTS.CREATED_AT)!!.toInstant(),
                    balance = Balance(
                        accountId = AccountId(r.get(BALANCES.ACCOUNT_ID)!!),
                        money = Money(
                            amount = r.get(BALANCES.AMOUNT)!!,
                            currency = Currency.valueOf(r.get(ACCOUNTS.CURRENCY)!!)
                        ),
                        updatedAt = r.get(BALANCES.UPDATED_AT)!!.toInstant()
                    )
                )
            }
    }

    override fun updateBalanceAmount(accountId: AccountId, newBalance: Money) {
        dsl.update(BALANCES)
            .set(BALANCES.AMOUNT, newBalance.amount)
            .set(BALANCES.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
            .where(BALANCES.ACCOUNT_ID.eq(accountId.value))
            .execute()
    }

    override fun updateStatus(accountId: AccountId, status: AccountStatus) {
        dsl.update(ACCOUNTS)
            .set(ACCOUNTS.STATUS, status.name)
            .where(ACCOUNTS.ACCOUNT_ID.eq(accountId.value))
            .execute()
    }

}