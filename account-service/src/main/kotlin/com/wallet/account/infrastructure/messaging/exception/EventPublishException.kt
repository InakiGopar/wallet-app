package com.wallet.account.infrastructure.messaging.exception

class EventPublishException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)