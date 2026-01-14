package com.wallet.account.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.utils.serializer.JacksonEventSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class JacksonEventSerializerTest {

    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    private val serializer = JacksonEventSerializer(objectMapper)

    @Test
    fun `should serialize BalanceUpdatedEvent correctly`() {
        val event = BalanceUpdatedEvent(
            accountId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            previousBalance = BigDecimal("100.00"),
            delta = BigDecimal("50.00"),
            newBalance = BigDecimal("150.00"),
            occurredAt = Instant.parse("2025-01-01T10:00:00Z")
        )

        val json = serializer.serialize(event)

        val tree = objectMapper.readTree(json)

        assertEquals(
            "11111111-1111-1111-1111-111111111111",
            tree["accountId"].asText()
        )
        assertEquals(
            0,
            tree["previousBalance"].decimalValue().compareTo(BigDecimal("100.00"))
        )
        assertEquals(
            0,
            tree["delta"].decimalValue().compareTo(BigDecimal("50.00"))
        )
        assertEquals(
            0,
            tree["newBalance"].decimalValue().compareTo(BigDecimal("150.00"))
        )

        assertEquals(
            "2025-01-01T10:00:00Z",
            tree["occurredAt"].asText()
        )
    }
}