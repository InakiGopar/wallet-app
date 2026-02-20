# 💳 Wallet Microservices – Event-Driven Banking Simulation

## 📖 Overview

This project is a **banking simulation platform** built using a **microservices architecture**.  
It models **bank accounts and financial transactions** that communicate asynchronously through **domain events**.

The system demonstrates:

- Event-Driven Architecture (EDA)
- Microservices communication via RabbitMQ
- Transactional Outbox Pattern
- Database versioning with Flyway
- Type-safe SQL access with jOOQ
- Integration testing using Testcontainers
- Containerized infrastructure with Docker

The purpose of this project is to simulate how distributed financial systems coordinate account balances and transactions reliably using asynchronous messaging.

---

# 🏗 Architecture

The system is composed of multiple microservices:

## 🧾 Account Service
- Manages bank accounts
- Handles balance updates
- Publishes account-related domain events

## 💸 Transaction Service
- Handles transaction lifecycle (create, approve, reject)
- Persists transaction state
- Publishes transaction events
- Listens to account-related events

---

## 🔄 Event-Driven Communication

Services communicate asynchronously via RabbitMQ queues.

### Example Flow

1. A transaction is created.
2. The `transaction-service` publishes a `TransactionCreatedEvent`.
3. The `account-service` consumes the event and validates balance.
4. It publishes either:
   - `TransactionApprovedEvent`
   - `TransactionRejectedEvent`
5. The `transaction-service` listens and updates the transaction status.

---

# 🧠 Key Architectural Concepts

## ✅ Microservices Architecture
Each service:
- Has its own database
- Is independently deployable
- Communicates via events

## ✅ Transactional Outbox Pattern
To guarantee reliable event publishing:
- Events are stored in an `outbox_event` table
- A dispatcher publishes pending events
- If publishing fails → event remains `PENDING`
- Ensures eventual consistency

## ✅ Database Per Service
Each microservice owns its schema and migrations.

---

# 🛠 Tech Stack

- Kotlin
- Spring Boot
- Flyway
- jOOQ
- RabbitMQ
- PostgreSQL
- Docker & Docker Compose
- Testcontainers
- JUnit 5
- Gradle

---

# 🧪 Testing Strategy

## Unit Tests
- Service layer logic
- Domain behavior
- Mocked dependencies

## Integration Tests
- Full Spring context
- Real PostgreSQL and RabbitMQ containers using Testcontainers
- Database isolation per test
- Transactional rollback when needed

Testcontainers ensures:
- No dependency on local DB
- Reproducible test environment
- CI-friendly setup

---

# 🐳 Running the Project Locally

## 1️⃣ Requirements

- Java 21+
- Docker
- Docker Compose
- Gradle

---

## 2️⃣ Start Infrastructure

```
docker compose up -d
```

## 3️⃣ Run Flyway Migrations
```
./gradlew flywayMigrate
```
## 4️⃣ Generate jOOQ Code
```
./gradlew generateJooq
```

## 5️⃣ Run the Services
```
./gradlew bootRun
```

### Or run each microservice individually:
```
./gradlew :account-service:bootRun
./gradlew :transaction-service:bootRun
```

# 🎯 Educational Purpose  
This project was built to:
- Practice backend architecture
- Understand distributed systems
- Learn event-driven patterns
- Master Kotlin + Spring Boot ecosystem
- Work with production-grade tooling (Flyway, jOOQ, RabbitMQ)
