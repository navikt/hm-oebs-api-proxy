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
    implementation(platform(libs.hotlibs.platform))

    // hotlibs
    implementation(libs.hotlibs.core)
    implementation(libs.hotlibs.http)
    implementation(libs.hotlibs.logging)
    implementation(libs.hotlibs.serialization)

    // Metrics
    implementation(libs.micrometer.registry.prometheus)

    // Ktor Server
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.client.apache)

    // Database
    implementation(libs.hotlibs.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:database-oracle")
        }
    }

    // GraphQL
    implementation(libs.graphql.ktor.client) {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization") // prefer jackson
        exclude("io.ktor", "ktor-client-serialization") // prefer ktor-client-jackson
    }
    implementation(libs.graphql.client.jackson)
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

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest(libs.versions.kotlin.asProvider())
            dependencies {
                implementation(libs.hotlibs.test)
                implementation(libs.kotest.assertions.ktor)
                implementation(libs.ktor.server.test.host)
                implementation(libs.hotlibs.database) {
                    capabilities {
                        requireCapability("no.nav.hjelpemidler:database-h2")
                    }
                }
            }
            targets.configureEach {
                testTask {
                    testLogging {
                        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                    }
                }
            }
        }
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

tasks.shadowJar { mergeServiceFiles() }
