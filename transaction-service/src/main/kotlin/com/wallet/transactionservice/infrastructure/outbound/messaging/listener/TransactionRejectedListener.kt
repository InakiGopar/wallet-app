package com.wallet.transactionservice.infrastructure.outbound.messaging.listener

import com.wallet.transactionservice.application.services.TransactionService
import com.wallet.transactionservice.domain.events.TransactionRejectedEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TransactionRejectedListener(
    private val transactionService: TransactionService
) {

    @RabbitListener(queues = ["transaction-service.transaction.rejected"])
    fun handle(event: TransactionRejectedEvent) {
        transactionService.markAsFailed(event.transactionId)
    }

}