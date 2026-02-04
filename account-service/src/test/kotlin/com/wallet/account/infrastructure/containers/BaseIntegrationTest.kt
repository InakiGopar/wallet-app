package com.wallet.account.infrastructure.containers

import com.wallet.account.jooq.tables.references.OUTBOX_EVENT
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BaseIntegrationTest {

    @Autowired
    lateinit var dsl: DSLContext

    @BeforeEach
    fun cleanDatabase() {
        dsl.deleteFrom(OUTBOX_EVENT).execute()
    }
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("account_db")
            .withUsername("test")
            .withPassword("test")
    }
}