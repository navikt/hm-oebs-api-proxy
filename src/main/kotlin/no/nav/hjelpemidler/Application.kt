package no.nav.hjelpemidler

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callid.CALL_ID_DEFAULT_DICTIONARY
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.routing.routing
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.database.createDataSource
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.models.SfFeil
import org.slf4j.event.Level

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    log.info { "Gjeldende miljø: ${Environment.current}, tier: ${Environment.current.tier}}" }
    environment.monitor.subscribe(ApplicationStarted) {
        // loggFeilendeSf()
    }
    installAuthentication()

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(jsonMapper))
    }

    install(CallLogging) {
        level = Level.TRACE
        filter { call ->
            !call.request.path().startsWith("/internal") &&
                !call.request.path().startsWith("/isalive") &&
                !call.request.path().startsWith("/isready") &&
                !call.request.path().startsWith("/metrics")
        }
        callIdMdc("correlationId")
    }

    install(CallId) {
        header(HttpHeaders.XCorrelationId)
        generate(10, CALL_ID_DEFAULT_DICTIONARY)
    }

    install(MicrometerMetrics) {
        registry = Prometheus.registry
    }

    val database = createDataSource {
        jdbcUrl = Configuration.OEBS_DB_JDBC_URL
        username = Configuration.OEBS_DB_USERNAME
        password = Configuration.OEBS_DB_PASSWORD
        driverClassName = "oracle.jdbc.OracleDriver"
        connectionTimeout = 1000
        idleTimeout = 10001
        maxLifetime = 30001
        maximumPoolSize = 10
        minimumIdle = 1
    }.let(::Database)

    routing {
        internal(database)
        hjelpemiddelsiden(database)
        saksbehandling(database)
        felles(database)
    }
}

fun ApplicationCall.getTokenInfo(): Map<String, JsonNode> = authentication
    .principal<JWTPrincipal>()
    ?.let { principal ->
        principal.payload.claims.entries
            .associate { claim -> claim.key to claim.value.`as`(JsonNode::class.java) }
    } ?: error("No JWT principal found in request")

private fun loggFeilendeSf() {
    log.info { "Henter feilende SF-er" }
    val listeAvFeilendeSf = emptyList<SfFeil>() // ServiceforespørselFeilDao().finnSfMedFeil()
    log.info { "Antall feilende SF: ${listeAvFeilendeSf.size}" }
    listeAvFeilendeSf.map {
        log.info { "Feilende SF: $it" }
    }
}
