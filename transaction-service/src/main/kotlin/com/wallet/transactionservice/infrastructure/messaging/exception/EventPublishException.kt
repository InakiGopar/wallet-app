package com.wallet.transactionservice.infrastructure.messaging.exception

class EventPublishException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
