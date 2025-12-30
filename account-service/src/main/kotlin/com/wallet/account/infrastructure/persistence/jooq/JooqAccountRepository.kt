package com.wallet.account.infrastructure.persistence.jooq

import com.wallet.account.domian.models.Account
import com.wallet.account.domian.models.AccountStatus
import com.wallet.account.domian.models.Balance
import com.wallet.account.infrastructure.persistence.jooq.extensions.toInstantUtc
import com.wallet.account.infrastructure.persistence.jooq.extensions.toLocalDateTimeUtc
import com.wallet.account.jooq.tables.references.ACCOUNTS
import com.wallet.account.jooq.tables.references.BALANCES
import com.wallet.account.repository.AccountRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID


@Repository
class JooqAccountRepository(
    private val dsl: DSLContext
): AccountRepository {

    override fun create(account: Account): Account {
        dsl.insertInto(ACCOUNTS)
            .set(ACCOUNTS.ACCOUNT_ID, account.accountId)
            .set(ACCOUNTS.CURRENCY, account.currency)
            .set(ACCOUNTS.STATUS, account.status.name)
            .set(ACCOUNTS.CREATED_AT, account.createdAt.toLocalDateTimeUtc())
            .execute()

        dsl.insertInto(BALANCES)
            .set(BALANCES.ACCOUNT_ID, account.accountId)
            .set(BALANCES.AMOUNT, account.balance.amount)
            .set(BALANCES.UPDATED_AT, account.balance.updatedAt.toLocalDateTimeUtc())
            .execute()

        return account
    }

    override fun findById(accountId: UUID): Account? {
        return dsl
            .select()
            .from(ACCOUNTS)
            .join(BALANCES)
            .on(BALANCES.ACCOUNT_ID.eq(ACCOUNTS.ACCOUNT_ID))
            .where(ACCOUNTS.ACCOUNT_ID.eq(accountId))
            .fetchOne { r ->
                Account(
                    accountId = r.get(ACCOUNTS.ACCOUNT_ID)!!,
                    currency = r.get(ACCOUNTS.CURRENCY)!!,
                    status = AccountStatus.valueOf(r.get(ACCOUNTS.STATUS)!!),
                    createdAt = r.get(ACCOUNTS.CREATED_AT)!!.toInstantUtc(),
                    balance = Balance(
                        accountId = r.get(BALANCES.ACCOUNT_ID)!!,
                        amount = r.get(BALANCES.AMOUNT)!!,
                        updatedAt = r.get(BALANCES.UPDATED_AT)!!.toInstantUtc()
                    )
                )
            }
    }

    override fun updateBalance(accountId: UUID, newAmount: BigDecimal) {
        dsl.update(BALANCES)
            .set(BALANCES.AMOUNT, newAmount)
            .set(BALANCES.UPDATED_AT, org.jooq.impl.DSL.currentLocalDateTime())
            .where(BALANCES.ACCOUNT_ID.eq(accountId))
            .execute()
    }

    override fun updateStatus(accountId: UUID, status: AccountStatus) {
        dsl.update(ACCOUNTS)
            .set(ACCOUNTS.STATUS, status.name)
            .where(ACCOUNTS.ACCOUNT_ID.eq(accountId))
            .execute()
    }

}