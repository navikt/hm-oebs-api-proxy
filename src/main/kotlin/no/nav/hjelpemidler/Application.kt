package no.nav.hjelpemidler

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.kafka.KafkaConsumerMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.HjelpemiddelBrukerOEBS
import no.nav.hjelpemidler.models.toHjelpemiddelBruker
import oracle.jdbc.OracleConnection
import oracle.jdbc.pool.OracleDataSource
import org.json.simple.JSONObject
import org.slf4j.event.Level
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.set
import kotlin.time.ExperimentalTime

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private var dbConnection: Connection? = null
private val ready = AtomicBoolean(false)

fun main(args: Array<String>) {
    logg.info("OEBS api proxy - Starting...")

    connectToOebsDB()

    // Serve http REST API requests
    EngineMain.main(args)

    // Note: We do not want to set ready=false again, dbConnection.isValid() will have kubernetes restart our entire pod as it is closed below.
    // and setting ready=false forces our /isAlive-endpoint to always say "ALIVE".

    // Cleanup
    logg.info("OEBS api proxy - Cleaning up and stopping.")
    dbConnection?.close()
}

fun connectToOebsDB() {
    // Clean up resources if we have already had a database connection set up here that has now failed
    ready.set(false)
    dbConnection = null

    // Set up a new connection
    try {
        /*sikkerlogg.info("Connecting to OEBS database with db-config-url=${Configuration.oracleDatabaseConfig["HM_OEBS_API_PROXY_DB_URL"]}, db-config-username=${Configuration.oracleDatabaseConfig["HM_OEBS_API_PROXY_DB_USR"]}")

        // Set up database connection
        val info = Properties()
        info[OracleConnection.CONNECTION_PROPERTY_USER_NAME] = Configuration.oracleDatabaseConfig["HM_OEBS_API_PROXY_DB_USR"]!!
        info[OracleConnection.CONNECTION_PROPERTY_PASSWORD] = Configuration.oracleDatabaseConfig["HM_OEBS_API_PROXY_DB_PW"]!!
        info[OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH] = "20"

        val ods = OracleDataSource()
        ods.url = Configuration.oracleDatabaseConfig["HM_OEBS_API_PROXY_DB_URL"]!!
        ods.connectionProperties = info

        logg.info("Connecting to database")
        dbConnection = ods.getConnection()

        logg.info("Fetching db metadata")
        val dbmd = dbConnection!!.metaData
        logg.info("Driver Name: " + dbmd.driverName)
        logg.info("Driver Version: " + dbmd.driverVersion)

        logg.info("Database connected, hm-oebs-api-proxy ready")
        */
        ready.set(true)

    }catch(e: Exception) {
        logg.info("Exception while connecting to database: $e")
        e.printStackTrace()
        throw e
    }
}

// Meant to fix "java.sql.SQLException: ORA-02399: overskred maks. tilkoblingstid, du blir logget av", by reconnection to the database and retrying
fun <T> withRetryIfDatabaseConnectionIsStale(block: () -> T): T {
    var lastException: SQLException? = null
    for (attempt in 1..3) { // We get three attempts
        try {
            return block() // Success
        }catch(e: SQLException) {
            lastException = e
            if (e.toString().contains("ORA-02399")) {
                logg.warn("Oracle database closed the connection due to their connection-max-life deadline, we reconnect and try again: $e")
                connectToOebsDB() // Reset database connection
                continue // Retry if we have attempts left
            }
            throw e // Unhandled sql error, we throw up
        }
    }
    throw lastException!! // No more attempts so we throw the last exception we had
}

// @ExperimentalTime
// @Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
fun Application.module() {
    installAuthentication()

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter())
    }

    install(CallLogging) {
        level = Level.TRACE
        filter { call ->
            !call.request.path().startsWith("/internal") &&
            !call.request.path().startsWith("/isalive") &&
            !call.request.path().startsWith("/isready")
        }
    }

    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            CollectorRegistry.defaultRegistry,
            Clock.SYSTEM
        )
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            LogbackMetrics(),
            KafkaConsumerMetrics()
        )
    }

    routing {
        // Endpoints for Kubernetes unauthenticated health checks

        get("/isalive") {
            // If we have gotten ready=true we check that dbConnection is still valid, or else we are ALIVE (so we don't get our pod restarted during startup)
            /* if (ready.get() ) {
                val dbValid = dbConnection!!.isValid(10)
                if (!dbValid) {
                    Prometheus.oebsDbAvailable.set(0.
                    0)
                    return@get call.respondText("NOT ALIVE", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
                }
                Prometheus.oebsDbAvailable.set(1.0)
            } */
            call.respondText("ALIVE", ContentType.Text.Plain)
        }

        get("/isready") {
            // if (!ready.get()) return@get call.respondText("NOT READY", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
            call.respondText("READY", ContentType.Text.Plain)
        }

        get("/metrics") {
            val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()

            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
            }
        }

        // Authenticated database proxy requests
        authenticate("tokenX") {
            get("/hjelpemidler-bruker") {
                // Extract FNR to lookup from idporten logon
                val fnr = call.getTokenInfo()["pid"]?.asText() ?: error("Could not find 'pid' claim in token")
                if (Configuration.application["APP_PROFILE"]!! != "prod") {
                    logg.info("Received request for /hjelpemidler-bruker on-behalf-of: $fnr")
                }

                val mock = listOf(
                    HjelpemiddelBrukerOEBS(
                        "177946",
                        "1234",
                        "1",
                        "",
                        "Rullator til innendørs bruk",
                        "I utlån",
                        "2000-01-01",
                        "",
                        "Gemino 20",
                        "1000",
                        "",
                        fnr,
                        "Installasjonsveien 1",
                        "Installasjonskommunen",
                        "1234",
                        "Installsjonsbyen",
                        "Bostedsveien 2",
                        "Bostedskommunen",
                        "4321",
                        "Bostedsbyen",
                    ).toHjelpemiddelBruker(),
                    HjelpemiddelBrukerOEBS(
                        "021922",
                        "2345",
                        "2",
                        "",
                        "Rullator til innendørs bruk",
                        "I utlån",
                        "2001-02-02",
                        "771044",
                        "Topro Troja Classic M",
                        "1001",
                        "",
                        fnr,
                        "Installasjonsveien 1",
                        "Installasjonskommunen",
                        "1234",
                        "Installsjonsbyen",
                        "Bostedsveien 2",
                        "Bostedskommunen",
                        "4321",
                        "Bostedsbyen",
                    ).toHjelpemiddelBruker(),
                    HjelpemiddelBrukerOEBS(
                        "014112",
                        "3456",
                        "5",
                        "",
                        "Terskeleliminator",
                        "I utlån",
                        "2002-03-03",
                        "",
                        "Topro Terskeleliminator",
                        "1002",
                        "",
                        fnr,
                        "Installasjonsveien 1",
                        "Installasjonskommunen",
                        "1234",
                        "Installsjonsbyen",
                        "Bostedsveien 2",
                        "Bostedskommunen",
                        "4321",
                        "Bostedsbyen",
                    ).toHjelpemiddelBruker(),
                )

                call.respond(mock)
            }
        }

        authenticate("aad") {
            get("/test") {
                val reqBody = call.receive<JSONObject>()
                call.respondText(reqBody.toJSONString(), ContentType.Application.Json, HttpStatusCode.OK)
            }
        }
    }
}

val Application.isLocal get() = Configuration.application["APP_PROFILE"]!! == "local"
val Application.isDev get() = Configuration.application["APP_PROFILE"]!! == "dev"
val Application.isProd get() = Configuration.application["APP_PROFILE"]!! == "prod"

fun ApplicationCall.getTokenInfo(): Map<String, JsonNode> = authentication
    .principal<JWTPrincipal>()
    ?.let { principal ->
        principal.payload.claims.entries
            .associate { claim -> claim.key to claim.value.`as`(JsonNode::class.java) }
    } ?: error("No JWT principal found in request")
