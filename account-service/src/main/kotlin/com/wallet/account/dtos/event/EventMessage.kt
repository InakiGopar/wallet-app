package com.wallet.account.dtos.event

import java.time.Instant

interface EventMessage {
    val occurredAt: Instant
}
