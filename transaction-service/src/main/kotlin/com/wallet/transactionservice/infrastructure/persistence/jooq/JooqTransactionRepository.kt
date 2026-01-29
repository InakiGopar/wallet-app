package com.wallet.transactionservice.infrastructure.persistence.jooq

import com.wallet.transaction.jooq.tables.references.TRANSACTIONS
import com.wallet.transactionservice.domain.models.AccountId
import com.wallet.transactionservice.domain.models.Transaction
import com.wallet.transactionservice.domain.models.TransactionId
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.Money
import com.wallet.transactionservice.domain.models.microTypes.TransactionStatus
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.domain.repository.TransactionRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.ZoneOffset

@Repository
class JooqTransactionRepository(
    private val dsl: DSLContext
) : TransactionRepository {

    override fun save(transaction: Transaction) {
        dsl.insertInto(TRANSACTIONS)
            .set(TRANSACTIONS.ID, transaction.transactionId.value)
            .set(TRANSACTIONS.ACCOUNT_ID, transaction.accountId.value)
            .set(TRANSACTIONS.AMOUNT, transaction.money.amount)
            .set(TRANSACTIONS.CURRENCY, transaction.money.currency.name)
            .set(TRANSACTIONS.TYPE, transaction.type.name)
            .set(TRANSACTIONS.STATUS, transaction.status.name)
            .set(
                TRANSACTIONS.CREATED_AT,
                transaction.createdAt.atOffset(ZoneOffset.UTC)
            )
            .execute()
    }

    override fun findById(id: TransactionId): Transaction? {
        return dsl.selectFrom(TRANSACTIONS)
            .where(TRANSACTIONS.ID.eq(id.value))
            .fetchOne { record ->
                Transaction(
                    transactionId = TransactionId(record.id!!),
                    accountId = AccountId(record.accountId!!),
                    money = Money(
                        amount = record.amount!!,
                        currency = Currency.valueOf(record.currency!!),
                    ),
                    type = TransactionType.valueOf(record.type!!),
                    status = TransactionStatus.valueOf(record.status!!),
                    createdAt = record.createdAt!!.toInstant(),
                )
            }
    }

}