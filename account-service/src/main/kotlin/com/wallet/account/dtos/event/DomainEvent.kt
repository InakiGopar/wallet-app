package com.wallet.account.dtos.event

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}
