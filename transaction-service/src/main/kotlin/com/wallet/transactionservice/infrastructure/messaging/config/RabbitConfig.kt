package com.wallet.transactionservice.infrastructure.messaging.config


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    companion object {
        const val WALLET_EXCHANGE = "wallet.events"
        const val BALANCE_UPDATED_QUEUE = "transaction.balance.updated"
        const val BALANCE_UPDATED_ROUTING_KEY = "balance.updated"
    }

    /**
     * Exchange common for all events
     */
    @Bean
    fun eventsExchange(): TopicExchange =
        TopicExchange(WALLET_EXCHANGE, true, false)

    /**
     * Queue of the transaction-service
     */
    @Bean
    fun transactionQueue(): Queue =
        Queue(BALANCE_UPDATED_QUEUE, true)

    /**
     * Binding: transaction-service consume 'balance.updated'
     */
    @Bean
    fun balanceUpdatedBinding(
        transactionQueue: Queue,
        eventsExchange: TopicExchange
    ): Binding =
        BindingBuilder
            .bind(transactionQueue)
            .to(eventsExchange)
            .with(BALANCE_UPDATED_ROUTING_KEY)


    @Bean
    fun jacksonMessageConverter(
        objectMapper: ObjectMapper
    ): Jackson2JsonMessageConverter =
        Jackson2JsonMessageConverter(objectMapper)

    // ========= Publishing =========

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter
    ): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }

    // ========= Consuming =========

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(messageConverter)
        }
}