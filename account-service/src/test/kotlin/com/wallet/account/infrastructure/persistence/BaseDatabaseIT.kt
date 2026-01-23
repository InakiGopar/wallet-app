package com.wallet.account.infrastructure.persistence

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.UUID


@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BaseDatabaseIT {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("account_db")
            .withUsername("test")
            .withPassword("test")
    }

    @Autowired
    lateinit var outboxRepository: OutboxRepository

    @Test
    fun `can save and retrieve data from database`() {
        // Arrange
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"accountId":"123","newBalance":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        // Act
        outboxRepository.save(event)
        val persisted = outboxRepository.findById(event.eventId)

        // Assert
        assertThat(persisted).isNotNull
        assertThat(persisted!!.status).isEqualTo(OutboxStatus.PENDING)
    }
}