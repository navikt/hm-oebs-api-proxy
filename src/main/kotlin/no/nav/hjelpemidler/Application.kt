package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.callid.CALL_ID_DEFAULT_DICTIONARY
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.client.NorgClient
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.database.Oracle
import no.nav.hjelpemidler.database.createDataSource
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.models.ServiceforespørselFeil
import no.nav.hjelpemidler.serialization.jackson.jsonMapper
import no.nav.hjelpemidler.service.NorgService
import org.slf4j.event.Level
import javax.sql.DataSource
import kotlin.time.Duration.Companion.minutes

private val log = KotlinLogging.logger {}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    TILLAT_SYNTETISKE_FØDSELSNUMRE = !Environment.current.isProd

    log.info { "Gjeldende miljø: ${Environment.current}, tier: ${Environment.current.tier}" }
    log.info { "Kobler til database: ${Configuration.OEBS_DB} med url: '${Configuration.OEBS_DB_JDBC_URL}'" }
    log.info { "Tillater syntetiske fødelsnumre: $TILLAT_SYNTETISKE_FØDSELSNUMRE" }

    /*
    monitor.subscribe(ApplicationStarted) {
        loggFeilendeServiceforespørsler()
    }
     */

    installAuthentication()
    installRouting(
        createDataSource(Oracle) {
            jdbcUrl = Configuration.OEBS_DB_JDBC_URL
            username = Configuration.OEBS_DB_USERNAME
            password = Configuration.OEBS_DB_PASSWORD
            connectionTimeout = 1.minutes.inWholeMilliseconds
        },
    )
}

fun Application.installRouting(dataSource: DataSource) {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(jsonMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            call.request.path() !in setOf(
                "/internal",
                "/isalive",
                "/isready",
                "/metrics",
            )
        }

        format { call ->
            "[${call.request.httpMethod.value}] ${call.request.uri}"
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

    val database = Database(dataSource)

    monitor.subscribe(ApplicationStopping) {
        database.close()
        monitor.unsubscribe(ApplicationStopping) {}
    }

    val norgClient = NorgClient()
    val norgService = NorgService(norgClient)

    routing {
        internal(database)
        hjelpemiddelsiden(database)
        saksbehandling(database)
        felles(database, norgService)
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
