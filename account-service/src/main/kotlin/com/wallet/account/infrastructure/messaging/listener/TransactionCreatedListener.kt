package com.wallet.account.infrastructure.messaging.listener

import com.wallet.account.domian.models.microTypes.AccountId
import com.wallet.account.domian.models.microTypes.Money
import com.wallet.account.dtos.event.TransactionCreatedEvent
import com.wallet.account.service.AccountService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TransactionCreatedListener(
    private val accountService: AccountService
) {
    @RabbitListener(queues = ["account.transaction.created"])
    fun handle(event: TransactionCreatedEvent) {

        val accountId = AccountId(event.accountId)
        val newAmount = Money(event.amount, event.currency)

        accountService.updateBalance(
            accountId,
            newAmount
        )
    }
}