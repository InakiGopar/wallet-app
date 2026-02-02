package com.wallet.transactionservice.utils.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.transactionservice.domain.models.microTypes.Currency
import com.wallet.transactionservice.domain.models.microTypes.TransactionType
import com.wallet.transactionservice.dtos.event.TransactionCreatedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest
class TransactionCreatedEventSerializerTest {
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var serializer: TransactionCreatedEventSerializer

    @Test
    fun `should serialize TransactionCreatedEvent to expected json`() {
        // given
        val event = TransactionCreatedEvent(
            transactionId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            accountId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            amount = BigDecimal("100.00"),
            currency = Currency.ARS,
            type = TransactionType.CREDIT.name,
            createdAt = Instant.parse("2026-01-01T10:00:00Z"),
            occurredAt = Instant.parse("2026-01-01T10:00:01Z")
        )

        val expectedJson = """
            {
              "transactionId": "00000000-0000-0000-0000-000000000001",
              "accountId": "00000000-0000-0000-0000-000000000002",
              "amount": 100.00,
              "currency": "ARS",
              "type": "CREDIT",
              "createdAt": "2026-01-01T10:00:00Z",
              "occurredAt": "2026-01-01T10:00:01Z"
            }
        """.trimIndent()


        // when
        val actualJson = serializer.serialize(event)

        // then
        assertEquals(
            objectMapper.readTree(expectedJson),
            objectMapper.readTree(actualJson)
        )
    }
}