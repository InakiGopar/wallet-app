package com.wallet.account.infrastructure.outbound.messaging.listener

import com.wallet.account.domian.models.AccountId
import com.wallet.account.domian.models.microTypes.BalanceDelta
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.dtos.event.TransactionCreatedEvent
import com.wallet.account.application.services.AccountService
import com.wallet.account.domian.models.microTypes.TransactionType
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TransactionCreatedListener(
    private val accountService: AccountService,
) {
    @RabbitListener(queues = ["account.transaction.created"])
    fun handle(event: TransactionCreatedEvent) {

        println("I listen: $event")
        //mapping the primitive types to domian types
        val transactionId = TransactionId(event.transactionId)
        val accountId = AccountId(event.accountId)
        val newAmount = BalanceDelta(event.amount)
        val type = TransactionType.valueOf(event.type)

        accountService.tryUpdateBalanceAmount(
            transactionId,
            accountId,
            newAmount,
            type
        )
    }
}