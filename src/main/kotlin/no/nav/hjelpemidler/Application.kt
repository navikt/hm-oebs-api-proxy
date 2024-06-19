package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStopping
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.database.Oracle
import no.nav.hjelpemidler.database.createDataSource
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.models.Fødselsnummer
import no.nav.hjelpemidler.models.ServiceforespørselFeil
import org.slf4j.event.Level

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    log.info { "Gjeldende miljø: ${Environment.current}, tier: ${Environment.current.tier}}" }

    /*
    environment.monitor.subscribe(ApplicationStarted) {
        loggFeilendeServiceforespørsler()
    }
     */

    installAuthentication()

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(jsonMapper))
    }

    install(CallLogging) {
        level = Level.TRACE
        filter { call ->
            val path = call.request.path()
            !path.startsWith("/internal") &&
                !path.startsWith("/isalive") &&
                !path.startsWith("/isready") &&
                !path.startsWith("/metrics")
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

    val database = createDataSource(Oracle) {
        jdbcUrl = Configuration.OEBS_DB_JDBC_URL
        username = Configuration.OEBS_DB_USERNAME
        password = Configuration.OEBS_DB_PASSWORD
    }.let(::Database)
    environment.monitor.subscribe(ApplicationStopping) {
        database.close()
    }

    routing {
        internal(database)
        hjelpemiddelsiden(database)
        saksbehandling(database)
        felles(database)
    }
}

/**
 * pid-claim fra pålogging med ID-porten
 */
val ApplicationCall.pid: Fødselsnummer
    get() = authentication.principal<JWTPrincipal>()
        ?.getClaim("pid", Fødselsnummer::class) ?: error("Token mangler pid-claim")

private fun loggFeilendeServiceforespørsler() = runBlocking(Dispatchers.IO) {
    log.info { "Henter feilende serviceforespørsler" }
    val serviceforespørselFeil =
        emptyList<ServiceforespørselFeil>() // ServiceforespørselFeilDao().finnFeilendeServiceforespørsler()
    log.info { "Antall feilende serviceforespørsler: ${serviceforespørselFeil.size}" }
    serviceforespørselFeil.forEach {
        log.info { "Feilende serviceforespørsel: $it" }
    }
}
