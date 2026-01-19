package com.wallet.account.infrastructure.messaging.dispatcher

import com.ninjasquad.springmockk.MockkBean
import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.infrastructure.messaging.exception.EventPublishException
import com.wallet.account.infrastructure.messaging.publisher.EventPublisher
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.UUID

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxDispatcherIT {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("wallet")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }


    @MockkBean
    lateinit var eventPublisher: EventPublisher

    @Autowired
    lateinit var dispatcher: OutboxDispatcher

    @Autowired
    lateinit var outboxRepository: OutboxRepository


    @Test
    fun `dispatch should publish pending events and mark them as SENT`() {
        val event = OutboxEvent(
            eventId = UUID.randomUUID(),
            aggregateId = UUID.randomUUID(),
            aggregateType = AggregateType.ACCOUNT,
            type = EventType.BALANCE_UPDATED,
            payload = """{"accountId":"123","newBalance":100}""",
            status = OutboxStatus.PENDING,
            occurredAt = Instant.now()
        )

        outboxRepository.save(event)

        every { eventPublisher.publish(any(), any()) } returns Unit

        dispatcher.dispatch()

        val persisted = outboxRepository.findById(event.eventId)!!

        assertThat(persisted.status).isEqualTo(OutboxStatus.SENT)

        verify {
            eventPublisher.publish("BALANCE_UPDATED", any())
        }
    }


    @Test
    fun `dispatch should mark event as FAILED when publishing fails`() {
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

        outboxRepository.save(event)

        every {
            eventPublisher.publish(any(), any())
        } throws EventPublishException("Rabbit down")

        // Act
        dispatcher.dispatch()

        // Assert
        val persisted = outboxRepository.findById(event.eventId)
        assertThat(persisted).isNotNull
        assertThat(persisted!!.status).isEqualTo(OutboxStatus.FAILED)
    }



}