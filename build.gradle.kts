import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLIntrospectSchemaTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.hjelpemidler"
version = "1.0-SNAPSHOT"

val logging_version: String by project
val logback_version: String by project
val konfig_version: String by project
val klaxon_version: String by project
val ojdbc_version: String by project
val unleash_version: String by project
val prometheus_version: String by project
val jackson_version: String by project
val junit_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.21"
    id("com.expediagroup.graphql") version "5.4.1"
    id("com.diffplug.spotless") version "6.6.1"
}

application {
    applicationName = "hm-oebs-api-proxy"
    mainClass.set("no.nav.hjelpemidler.ApplicationKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.guepardoapps:kulid:2.0.0.0")
    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.0")

    // Logging
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1") {
        exclude("com.fasterxml.jackson.core")
    }

    // Jackson
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.3")

    // GraphQL
    implementation("com.expediagroup:graphql-kotlin-ktor-client:5.4.1") {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization") // prefer jackson
        exclude("io.ktor", "ktor-client-serialization") // prefer ktor-client-jackson
        exclude("io.ktor", "ktor-client-cio") // prefer ktor-client-apache
    }
    implementation("com.expediagroup:graphql-kotlin-client-jackson:5.4.1")

    // Database
    implementation("com.github.seratch:kotliquery:1.7.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.oracle.database.jdbc:ojdbc11:21.5.0.0")

    // Ktor
    implementation("io.ktor:ktor-jackson:1.6.4")
    implementation("io.ktor:ktor-auth:1.6.4")
    implementation("io.ktor:ktor-auth-jwt:1.6.4")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.4")

    // Ktor Server
    implementation("io.ktor:ktor-server-core:1.6.4")
    implementation("io.ktor:ktor-server-cio:1.6.4")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:1.6.4")
    implementation("io.ktor:ktor-client-apache:1.6.4")
    implementation("io.ktor:ktor-client-jackson:1.6.4")
    implementation("io.ktor:ktor-client-auth:1.6.4")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.testcontainers:oracle-xe:1.17.1")
}

spotless {
    kotlin {
        ktlint()
        targetExclude("build/generated/source/**/*")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
        outputs.upToDateWhen { false }
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4.2"
}

tasks.named("compileKotlin") {
    dependsOn("spotlessApply")
    dependsOn("spotlessCheck")
}

graphql {
    client {
        schemaFile = file("src/main/resources/hmdb/schema.graphql")
        queryFileDirectory = "src/main/resources/hmdb"
        packageName = "no.nav.hjelpemidler.client.hmdb"
    }
}

val graphqlIntrospectSchema by tasks.getting(GraphQLIntrospectSchemaTask::class) {
    endpoint.set("https://hm-grunndata-api.dev.intern.nav.no/graphql")
    // endpoint.set("http://localhost:8880/graphql")
    outputFile.set(file("src/main/resources/hmdb/schema.graphql"))
}
