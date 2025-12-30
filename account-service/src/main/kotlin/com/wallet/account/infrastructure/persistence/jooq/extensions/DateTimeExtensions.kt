package com.wallet.account.infrastructure.persistence.jooq.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/*
* Converts LocalDate(DB) to Instant (Domain) and vice versa
* */

fun LocalDateTime.toInstantUtc(): Instant =
    this.atZone(ZoneOffset.UTC).toInstant()

fun Instant.toLocalDateTimeUtc(): LocalDateTime =
    LocalDateTime.ofInstant(this, ZoneOffset.UTC)