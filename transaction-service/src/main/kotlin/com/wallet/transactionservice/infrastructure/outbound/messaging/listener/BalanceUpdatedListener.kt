package com.wallet.transactionservice.infrastructure.outbound.messaging.listener

import com.wallet.transactionservice.dtos.event.BalanceUpdatedEvent
import com.wallet.transactionservice.application.services.TransactionService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class BalanceUpdatedListener(
    private val transactionService: TransactionService
) {
    @RabbitListener(
        queues = ["transaction.balance.updated"]
    )
    fun handle(event: BalanceUpdatedEvent) {
        transactionService.markAsCompleted(event)
    }
}