package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.callid.CALL_ID_DEFAULT_DICTIONARY
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache5.Apache5
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson3.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import no.nav.hjelpemidler.client.GrunndataClient
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.collections.enumSetOf
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.database.Oracle
import no.nav.hjelpemidler.database.createDataSource
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.http.openid.IdentityProvider
import no.nav.hjelpemidler.http.openid.Texas
import no.nav.hjelpemidler.http.openid.TexasClient
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.norg.NorgClient
import no.nav.hjelpemidler.serialization.jackson.jsonMapper
import no.nav.hjelpemidler.server.ktor.security.AutentisertInnbygger
import no.nav.hjelpemidler.server.ktor.security.AuthenticationProviderName
import no.nav.hjelpemidler.server.ktor.security.authenticatedPrincipal
import no.nav.hjelpemidler.server.ktor.security.texas
import no.nav.hjelpemidler.service.HjelpemiddeloversiktService
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
    log.info { "Tillater syntetiske fødelsnumre: $TILLAT_SYNTETISKE_FØDSELSNUMRE" }
    if (!Environment.current.isProd) {
        log.info { "Kobler til database: ${Configuration.OEBS_MILJO} med url: '${Configuration.OEBS_DB_JDBC_URL}', bruker: ${Configuration.OEBS_DB_USERNAME}" }
    } else {
        log.info { "Kobler til database: ${Configuration.OEBS_MILJO} med url: '${Configuration.OEBS_DB_JDBC_URL}'" }
    }

    dependencies {
        provide<HttpClientEngine> { Apache5.create() }
        provide<Texas> { TexasClient(engine = resolve()) }

        provide<DataSource> {
            createDataSource(Oracle) {
                jdbcUrl = Configuration.OEBS_DB_JDBC_URL
                username = Configuration.OEBS_DB_USERNAME
                password = Configuration.OEBS_DB_PASSWORD
                connectionTimeout = 1.minutes.inWholeMilliseconds
            }
        }
        provide(::Database)

        provide { GrunndataClient(engine = resolve()) }
        provide { NorgClient(engine = resolve()) }
        provide { OebsApiClient(engine = resolve()) }

        provide(::HjelpemiddeloversiktService)
        provide(::NorgService)
    }

    val texas: Texas by dependencies
    authentication {
        texas(
            texas,
            name = AuthenticationProviderName.ENTRA_ID,
            identityProviders = enumSetOf(IdentityProvider.ENTRA_ID),
        )
        texas(
            texas,
            name = AuthenticationProviderName.TOKEN_X,
            identityProviders = enumSetOf(IdentityProvider.TOKEN_X),
        )
    }

    installRouting()
}

fun Application.installRouting() {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(jsonMapper))
    }

    install(CallLogging) {
        level = Level.TRACE
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

    routing {
        internal()
        hjelpemiddelsiden()
        saksbehandling()
        kommuneApi()
        felles()
    }
}

/**
 * pid-claim fra pålogging med ID-porten
 */
val ApplicationCall.pid: Fødselsnummer
    get() = (authenticatedPrincipal() as? AutentisertInnbygger)?.fnr ?: error("Token mangler pid-claim")
