package com.wallet.account.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.utils.serializer.JacksonEventSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
            transactionId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
            accountId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            previousBalance = BigDecimal("100.00"),
            delta = BigDecimal("50.00"),
            newBalance = BigDecimal("150.00"),
            occurredAt = Instant.parse("2025-01-01T10:00:00Z")
        )

        val json = serializer.serialize(event)

        val tree = objectMapper.readTree(json)


        assertEquals(
            "00000000-0000-0000-0000-000000000000",
            tree["transactionId"].asText()
        )

        assertEquals(
            "11111111-1111-1111-1111-111111111111",
            tree["accountId"].asText()
        )
        assertTrue(
            tree["previousBalance"].decimalValue()
                .compareTo(BigDecimal("100.00")) == 0,
            "previousBalance should be 100.00"
        )
        assertTrue(
            tree["delta"].decimalValue()
                .compareTo(BigDecimal("50.00")) == 0,
            "delta should be 50.00"
        )

        assertTrue(
            tree["newBalance"].decimalValue()
                .compareTo(BigDecimal("150.00")) == 0,
            "newBalance should be 150.00"
        )

        assertEquals(
            "2025-01-01T10:00:00Z",
            tree["occurredAt"].asText()
        )
    }
}