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
    runtimeOnly(libs.bundles.logging.runtime)

    // Jackson
    implementation(libs.bundles.jackson)

    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Database
    implementation(libs.hm.database)
    implementation(libs.hm.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:hm-database-oracle")
        }
    }

    // GraphQL
    implementation(libs.graphql.ktor.client) {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization") // prefer jackson
        exclude("io.ktor", "ktor-client-serialization") // prefer ktor-client-jackson
    }
    implementation(libs.graphql.client.jackson)

    // Testing
    testImplementation(libs.bundles.ktor.server.test)
    testImplementation(libs.hm.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:hm-database-h2")
        }
    }
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
