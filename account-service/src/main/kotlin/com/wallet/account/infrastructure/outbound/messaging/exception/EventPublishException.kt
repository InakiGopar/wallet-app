package com.wallet.account.infrastructure.outbound.messaging.exception

class EventPublishException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)