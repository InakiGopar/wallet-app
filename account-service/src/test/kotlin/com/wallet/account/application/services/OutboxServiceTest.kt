package com.wallet.account.application.services

import com.wallet.account.domian.events.AggregateType
import com.wallet.account.domian.events.EventType
import com.wallet.account.domian.events.OutboxEvent
import com.wallet.account.domian.events.OutboxStatus
import com.wallet.account.domian.events.RejectionReason
import com.wallet.account.domian.events.TransactionRejectedEvent
import com.wallet.account.domian.models.microTypes.TransactionId
import com.wallet.account.domian.repository.OutboxRepository
import com.wallet.account.dtos.event.BalanceUpdatedEvent
import com.wallet.account.utils.serializer.EventSerializer
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OutboxServiceTest {

    @MockK
    lateinit var outboxRepository: OutboxRepository

    @MockK
    lateinit var balanceUpdatedEventSerializer: EventSerializer<BalanceUpdatedEvent>

    @MockK
    lateinit var transactionRejectedSerializer: EventSerializer<TransactionRejectedEvent>

    @InjectMockKs
    lateinit var outboxService: OutboxService



    @Test
    fun `should serialize payload and save outbox event`() {

        val aggregateId = UUID.randomUUID()
        val payload = BalanceUpdatedEvent(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            previousBalance = BigDecimal("200"),
            delta = BigDecimal("100"),
            newBalance = BigDecimal("300"),
            occurredAt = Instant.now()
        )

        val serializedJson = """{"mock":"json"}"""

        every { balanceUpdatedEventSerializer.serialize(payload) } returns serializedJson
        every { outboxRepository.save(any()) } just Runs

        val slot = slot<OutboxEvent>()

        outboxService.registerBalanceUpdatedEvent(
            aggregateId = aggregateId,
            aggregateType = AggregateType.ACCOUNT,
            eventType = EventType.BALANCE_UPDATED,
            payload = payload
        )

        verify(exactly = 1) {
            balanceUpdatedEventSerializer.serialize(payload)
        }

        verify {
            outboxRepository.save(capture(slot))
        }

        val saved = slot.captured

        assertEquals(aggregateId, saved.aggregateId)
        assertEquals(EventType.BALANCE_UPDATED, saved.type)
        assertEquals(serializedJson, saved.payload)
        assertEquals(OutboxStatus.PENDING, saved.status)
        assertNotNull(saved.eventId)
        assertNotNull(saved.occurredAt)
    }

    @Test
    fun `should serialize and save rejected event`() {

        val transactionId = TransactionId(UUID.randomUUID())
        val reason = RejectionReason.INSUFFICIENT_FUNDS

        val serializedJson = """{"reason":"INSUFFICIENT_FUNDS"}"""

        every { transactionRejectedSerializer.serialize(any()) } returns serializedJson
        every { outboxRepository.save(any()) } just Runs

        val slot = slot<OutboxEvent>()

        outboxService.registerRejectedEvent(transactionId, reason)

        verify {
            transactionRejectedSerializer.serialize(any())
        }

        verify {
            outboxRepository.save(capture(slot))
        }

        val saved = slot.captured

        assertEquals(EventType.TRANSACTION_REJECTED, saved.type)
        assertEquals(AggregateType.ACCOUNT, saved.aggregateType)
        assertEquals(transactionId.value, saved.aggregateId)
        assertEquals(serializedJson, saved.payload)
        assertEquals(OutboxStatus.PENDING, saved.status)
    }


}