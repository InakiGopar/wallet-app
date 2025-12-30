package com.wallet.account.infrastructure.messaging.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitConfig {

    companion object {
        const val WALLET_EXCHANGE = "wallet.events"
        const val TRANSACTION_CREATED_QUEUE = "account.transaction.created"
        const val TRANSACTION_CREATED_ROUTING_KEY = "transaction.created"
    }

    @Bean
    fun walletExchange(): TopicExchange =
        TopicExchange(WALLET_EXCHANGE, true, false)

    @Bean
    fun transactionCreatedQueue(): Queue =
        Queue(TRANSACTION_CREATED_QUEUE, true)

    @Bean
    fun transactionCreatedBinding(
        queue: Queue,
        exchange: TopicExchange
    ): Binding =
        BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(TRANSACTION_CREATED_ROUTING_KEY)
}