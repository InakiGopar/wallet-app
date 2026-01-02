package com.wallet.account.infrastructure.persistence.jooq

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.microTypes.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.Currency
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.infrastructure.persistence.jooq.extensions.toInstantUtc
import com.wallet.account.infrastructure.persistence.jooq.extensions.toLocalDateTimeUtc
import com.wallet.account.jooq.tables.references.ACCOUNTS
import com.wallet.account.jooq.tables.references.BALANCES
import com.wallet.account.domian.repository.AccountRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository



@Repository
class JooqAccountRepository(
    private val dsl: DSLContext
): AccountRepository {

    override fun create(account: Account): Account {
        dsl.insertInto(ACCOUNTS)
            .set(ACCOUNTS.ACCOUNT_ID, account.accountId.value)
            .set(ACCOUNTS.CURRENCY, account.currency.name)
            .set(ACCOUNTS.STATUS, account.status.name)
            .set(ACCOUNTS.CREATED_AT, account.createdAt.toLocalDateTimeUtc())
            .execute()

        dsl.insertInto(BALANCES)
            .set(BALANCES.ACCOUNT_ID, account.accountId.value)
            .set(BALANCES.AMOUNT, account.balance.money.amount)
            .set(BALANCES.UPDATED_AT, account.balance.updatedAt.toLocalDateTimeUtc())
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
                    createdAt = r.get(ACCOUNTS.CREATED_AT)!!.toInstantUtc(),
                    balance = Balance(
                        accountId = AccountId(r.get(BALANCES.ACCOUNT_ID)!!),
                        money = Money(
                            amount = r.get(BALANCES.AMOUNT)!!,
                            currency = Currency.valueOf(r.get(ACCOUNTS.CURRENCY)!!)
                        ),
                        updatedAt = r.get(BALANCES.UPDATED_AT)!!.toInstantUtc()
                    )
                )
            }
    }

    override fun updateBalance(accountId: AccountId, newAmount: Money) {
        dsl.update(BALANCES)
            .set(BALANCES.AMOUNT, newAmount.amount)
            .set(BALANCES.UPDATED_AT, org.jooq.impl.DSL.currentLocalDateTime())
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