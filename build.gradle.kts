
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLIntrospectSchemaTask
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graphql)
    alias(libs.plugins.spotless)
}

application {
    applicationName = "hm-oebs-api-proxy"
    mainClass.set("no.nav.hjelpemidler.ApplicationKt")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.hm.http)

    // Logging
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.logstash.logback.encoder) {
        exclude("com.fasterxml.jackson.core")
    }

    // Jackson
    implementation(libs.jackson.datatype.jsr310)

    // GraphQL
    implementation(libs.graphql.kotlin.ktor.client) {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization") // prefer jackson
        exclude("io.ktor", "ktor-client-serialization") // prefer ktor-client-jackson
    }
    implementation(libs.graphql.kotlin.client.jackson)

    // Database
    implementation(libs.kotliquery)
    implementation(libs.hikaricp)
    runtimeOnly(libs.ojdbc11)

    // Ktor
    implementation(libs.ktor.serialization.jackson)

    // Ktor Server
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.ktor.server.netty)

    // Testing
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.client.mock)
    testRuntimeOnly(libs.h2)
}

spotless {
    kotlin {
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_value-argument-comment" to "disabled",
            ),
        )
        targetExclude("build/generated/source/**/*")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

kotlin { jvmToolchain(21) }

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

graphql {
    client {
        schemaFile = file("src/main/resources/hmdb/schema.graphqls")
        queryFileDirectory = "src/main/resources/hmdb"
        packageName = "no.nav.hjelpemidler.client.hmdb"
    }
}

val graphqlIntrospectSchema by tasks.getting(GraphQLIntrospectSchemaTask::class) {
    endpoint.set("https://hm-grunndata-search.intern.dev.nav.no/graphql")
    // endpoint.set("http://localhost:8880/graphql")
    outputFile.set(file("src/main/resources/hmdb/schema.graphqls"))
}
