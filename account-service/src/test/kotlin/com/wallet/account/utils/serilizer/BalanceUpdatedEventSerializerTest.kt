package com.wallet.account.utils.serilizer

import com.fasterxml.jackson.databind.ObjectMapper
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.utils.serializer.BalanceUpdatedEventSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest
class BalanceUpdatedEventSerializerTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var serializer: BalanceUpdatedEventSerializer

    @Test
    fun `should serialize BalanceUpdatedEvent to expected json`() {
        val transactionId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val accountId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        val occurredAt = Instant.parse("2026-01-01T10:00:01Z")

        val event = BalanceUpdatedEvent(
            transactionId = transactionId,
            accountId = accountId,
            previousBalance = BigDecimal("100.00"),
            delta = BigDecimal("50.00"),
            newBalance = BigDecimal("150.00"),
            occurredAt = occurredAt
        )

        val expectedJson = """
        {
          "transactionId": "$transactionId",
          "accountId": "$accountId",
          "previousBalance": 100.00,
          "delta": 50.00,
          "newBalance": 150.00,
          "occurredAt": "2026-01-01T10:00:01Z"
        }
    """.trimIndent()

        val actualJson = serializer.serialize(event)

        assertEquals(
            objectMapper.readTree(expectedJson),
            objectMapper.readTree(actualJson)
        )
    }
}