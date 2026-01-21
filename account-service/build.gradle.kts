
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

    // JOOQ
    id("nu.studer.jooq") version "9.0"

    //Flyway
    id("org.flywaydb.flyway") version "10.20.0"
}

dependencyManagement {
    imports {
        mavenBom("org.jooq:jooq-bom:3.19.28")
    }
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
    implementation("org.jooq:jooq")

    // ⚙️ jOOQ codegen
    jooqGenerator("org.jooq:jooq-codegen")
    jooqGenerator("org.jooq:jooq-meta")
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

    // 🧪 Tests unitarios
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")

    // Testcontainers
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}


tasks.withType<Test> {
    useJUnitPlatform()
    exclude("**/*IT.class") //exclude integration test
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


flyway {
    url = "jdbc:postgresql://localhost:5432/account_db"
    user = "postgres"
    password = ""
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

tasks.register("dbCodegen") {
    group = "database"
    description = "Runs Flyway migrations and then generates jOOQ sources"
    dependsOn("flywayMigrate", "generateJooq")
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests with Testcontainers"

    useJUnitPlatform()

    // only integration tests
    include("**/*IT.class")

    environment("TESTCONTAINERS_RYUK_DISABLED", "false")
    environment(
        "DOCKER_HOST",
        System.getenv("DOCKER_HOST") ?: "unix:///var/run/docker.sock"
    )

    shouldRunAfter(tasks.test)
}


