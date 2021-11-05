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
val ktor_version: String by project
val unleash_version: String by project
val prometheus_version: String by project
val jackson_version: String by project
val junit_version: String by project

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(GraphQL.graphql) version GraphQL.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

apply {
    plugin(Spotless.spotless)
}

application {
    applicationName = "hm-oebs-api-proxy"
    mainClassName = "no.nav.hjelpemidler.ApplicationKt"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // Used for Rapids and rivers-dependency
    maven("https://packages.confluent.io/maven/") // Kafka-avro
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api("ch.qos.logback:logback-classic:1.2.6")
    api("net.logstash.logback:logstash-logback-encoder:6.6") {
        exclude("com.fasterxml.jackson.core")
    }

    implementation(Jackson.core)
    implementation(Jackson.kotlin)
    implementation(Jackson.jsr310)
    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Fuel.fuel)
    implementation(Fuel.library("coroutines"))
    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.guepardoapps:kulid:1.1.2.0")

    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")
    implementation("io.ktor:ktor-client-auth-jvm:$ktor_version")
    implementation("no.finn.unleash:unleash-client-java:$unleash_version")
    implementation(Micrometer.prometheusRegistry)
    implementation(GraphQL.ktorClient) {
        exclude("com.expediagroup", "graphql-kotlin-client-serialization") // prefer jackson
        exclude("io.ktor", "ktor-client-serialization") // prefer ktor-client-jackson
        exclude("io.ktor", "ktor-client-cio") // prefer ktor-client-apache
    }
    implementation(GraphQL.clientJackson)

    // hm-oebs-api-proxy bibloteker
    implementation(Database.Kotlinquery)
    implementation(Database.HikariCP)
    implementation("com.beust:klaxon:$klaxon_version")
    // implementation("com.oracle.database.jdbc:ojdbc8:$ojdbc_version")
    implementation("com.oracle.database.jdbc:ojdbc11:$ojdbc_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")

    implementation("io.ktor:ktor-server-core:$ktor_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")

    testImplementation(Kotlin.testJUnit5)
    testImplementation(KoTest.assertions)
    testImplementation(KoTest.runner)
    testImplementation(Ktor.ktorTest)
    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
    testImplementation("org.testcontainers:oracle-xe:1.16.2")
    testImplementation(Wiremock.standalone)

    // testImplementation(kotlin("testJUnit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.gradle.kts")
        ktlint(Ktlint.version)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf()
    kotlinOptions.jvmTarget = "11"
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

tasks.withType<Wrapper> {
    gradleVersion = "7.2"
}

tasks.named("shadowJar") {
    dependsOn("test")
}

tasks.named("jar") {
    dependsOn("test")
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
