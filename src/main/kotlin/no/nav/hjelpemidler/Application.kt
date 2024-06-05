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
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselFeilDao
import org.slf4j.event.Level

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    logg.info { "Gjeldende miljø: ${Environment.current}, tier: ${Environment.current.tier}}" }
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

    routing {
        internal()
        hjelpemiddelsiden()
        saksbehandling()
        felles()
    }
}

fun ApplicationCall.getTokenInfo(): Map<String, JsonNode> = authentication
    .principal<JWTPrincipal>()
    ?.let { principal ->
        principal.payload.claims.entries
            .associate { claim -> claim.key to claim.value.`as`(JsonNode::class.java) }
    } ?: error("No JWT principal found in request")

private fun loggFeilendeSf() {
    logg.info { "Henter feilende SF-er" }
    val listeAvFeilendeSf = ServiceforespørselFeilDao().finnSfMedFeil()
    logg.info { "Antall feilende SF: ${listeAvFeilendeSf.size}" }
    listeAvFeilendeSf.map {
        logg.info { "Feilende SF: $it" }
    }
}
