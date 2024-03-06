import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateClientTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLIntrospectSchemaTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.hjelpemidler"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    id("com.expediagroup.graphql") version "7.0.2"
    id("com.diffplug.spotless") version "6.25.0"
}

application {
    applicationName = "hm-oebs-api-proxy"
    mainClass.set("no.nav.hjelpemidler.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.3")

    // Logging
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.1")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.4") {
        exclude("com.fasterxml.jackson.core")
    }

    // Jackson
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")

    // GraphQL
    val graphQLVersion = "7.0.2"
    implementation("com.expediagroup:graphql-kotlin-ktor-client:$graphQLVersion") {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization") // prefer jackson
        exclude("io.ktor", "ktor-client-serialization") // prefer ktor-client-jackson
        exclude("io.ktor", "ktor-client-cio") // prefer ktor-client-apache
    }
    implementation("com.expediagroup:graphql-kotlin-client-jackson:$graphQLVersion")

    // Database
    implementation("com.github.seratch:kotliquery:1.9.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")

    // Ktor
    implementation("io.ktor:ktor-serialization-jackson")

    // Ktor Server
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-cio-jvm")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-metrics-micrometer")

    // Ktor Client
    implementation("io.ktor:ktor-client-apache-jvm")
    implementation("io.ktor:ktor-client-auth-jvm")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-jackson-jvm")
    implementation("io.ktor:ktor-client-logging-jvm")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock-jvm")
    testImplementation("org.testcontainers:oracle-xe:1.19.7")
    constraints {
        implementation("org.apache.commons:commons-compress:1.26.0")
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

kotlin {
    jvmToolchain(21)
}

tasks.test {
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

tasks.withType<KotlinCompile> {
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
    endpoint.set("https://hm-grunndata-api.intern.dev.nav.no/graphql")
    // endpoint.set("http://localhost:8880/graphql")
    outputFile.set(file("src/main/resources/hmdb/schema.graphql"))
}

// Add secondary hmdb client
val graphqlGenerateOtherClient by tasks.registering(GraphQLGenerateClientTask::class) {
    packageName.set("no.nav.hjelpemidler.client.hmdbng")
    schemaFile.set(file("src/main/resources/hmdbng/schema.graphqls"))
    queryFileDirectory.set(file("${project.projectDir.absolutePath}/src/main/resources/hmdbng"))
    outputDirectory.set(file(project.layout.buildDirectory.dir("generated/source/graphql/main")))
}

tasks {
    // original client generation task will automatically add itself to compileKotlin dependency
    // make sure that before we run compile task it will generate other client as well
    compileKotlin {
        dependsOn("graphqlGenerateOtherClient")
    }
}
