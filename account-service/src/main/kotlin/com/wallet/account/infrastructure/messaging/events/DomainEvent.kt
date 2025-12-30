package com.wallet.account.infrastructure.messaging.events

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}
