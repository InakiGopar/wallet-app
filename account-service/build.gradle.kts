
repositories {
    mavenCentral()
}


plugins {
    // Spring
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"

    // Kotlin
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.21"

    // jOOQ
    id("nu.studer.jooq") version "9.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val jooqVersion = "3.19.28"

dependencies {

    // 🌱 Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // 🧠 Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // 🧬 jOOQ runtime
    implementation("org.jooq:jooq:$jooqVersion")

    // ⚙️ jOOQ codegen
    jooqGenerator("org.jooq:jooq-codegen:$jooqVersion")
    jooqGenerator("org.jooq:jooq-meta:$jooqVersion")
    jooqGenerator("org.postgresql:postgresql:42.7.3")

    // 🛫 Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // 🐘 PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // 🐇 RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // 📛 Zalando Problem
    implementation("org.zalando:problem-spring-web-starter:0.29.1")

    // 🧪 Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            jooqConfiguration.apply {

                logging = org.jooq.meta.jaxb.Logging.WARN

                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/account_db"
                    user = "postgres"
                    password = ""
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }

                    generate.apply {
                        isDaos = false
                        isPojos = true
                        isImmutablePojos = true
                        isFluentSetters = false
                    }

                    target.apply {
                        packageName = "com.wallet.account.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}
