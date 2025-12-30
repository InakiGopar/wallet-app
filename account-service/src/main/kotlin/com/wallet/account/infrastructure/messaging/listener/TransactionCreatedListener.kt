package com.wallet.account.infrastructure.messaging.listener

import com.wallet.account.infrastructure.messaging.events.TransactionCreatedEvent
import com.wallet.account.service.AccountService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TransactionCreatedListener(
    private val accountService: AccountService
) {
    @RabbitListener(queues = ["account.transaction.created"])
    fun handle(event: TransactionCreatedEvent) {
        accountService.updateBalance(
            event.accountId,
            event.amount,
        )
    }
}