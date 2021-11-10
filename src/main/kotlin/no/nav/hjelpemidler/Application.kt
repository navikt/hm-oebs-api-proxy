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
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
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
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.service.oebsdatabase.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.oebsdatabase.PersoninformasjonDao
import no.nav.hjelpemidler.service.oebsdatabase.TittelForHmsnrDao
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselDao
import org.slf4j.event.Level
import kotlin.time.ExperimentalTime

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val hjelpemiddeloversiktDao = HjelpemiddeloversiktDao()
private val tittleForHmsnrDao = TittelForHmsnrDao()
private val personinformasjonDao = PersoninformasjonDao()
private val opprettServiceforespørselDao = ServiceforespørselDao()

fun main(args: Array<String>) = EngineMain.main(args)

@ExperimentalTime
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
                    !call.request.path().startsWith("/isready") &&
                    !call.request.path().startsWith("/metrics")
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
            // Let's check if the datasource has been closed
            if (Configuration.dataSource.isClosed) {
                return@get call.respondText("NOT ALIVE", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
            }
            call.respondText("ALIVE", ContentType.Text.Plain)
        }

        get("/isready") {
            // Let's check if the datasource is still valid and working
            if (!Configuration.dataSource.getConnection().isValid(10)) {
                Prometheus.oebsDbAvailable.set(0.0)
                return@get call.respondText("NOT READY", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
            }
            Prometheus.oebsDbAvailable.set(1.0)
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
                    logg.info("Processing request for /hjelpemidler-bruker (on-behalf-of: $fnr)")
                } else {
                    logg.info("Processing request for /hjelpemidler-bruker")
                }

                // Extra sanity check of FNR
                if (!"\\d{11}".toRegex().matches(fnr)) {
                    error("invalid fnr in 'pid', does not match regex")
                }

                call.respond(hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr))
            }
        }

        authenticate("aad") {

            post("/opprettSF") {
                try {
                    val sf = call.receive<Serviceforespørsel>()
                    opprettServiceforespørselDao.opprettServiceforespørsel(sf)
                    logg.info("Serviceforspørsel for sak ${sf.referansenummer} opprettet")
                    call.respond(HttpStatusCode.Created)
                } catch (e: Exception) {
                    logg.error("Noe gikk feil med opprettelse av SF", e)
                    throw e
                }
            }

            post("/getLeveringsaddresse") {
                val fnr = call.receiveText()
                // Extra sanity check of FNR
                if (!"\\d{11}".toRegex().matches(fnr)) {
                    error("invalid fnr in 'pid', does not match regex")
                }
                val personinformasjonListe = personinformasjonDao.hentPersoninformasjon(fnr)
                call.respond(personinformasjonListe)
            }

            get("/get-title-for-hmsnr/{hmsNr}") {
                val result = tittleForHmsnrDao.hentTittelForHmsnr(call.parameters["hmsNr"]!!)
                if (result == null) {
                    call.respond(HttpStatusCode.NotFound, """{"error": "product or accessory not found"}""")
                    return@get
                }
                call.respond(result)
            }
        }
    }
}

fun ApplicationCall.getTokenInfo(): Map<String, JsonNode> = authentication
    .principal<JWTPrincipal>()
    ?.let { principal ->
        principal.payload.claims.entries
            .associate { claim -> claim.key to claim.value.`as`(JsonNode::class.java) }
    } ?: error("No JWT principal found in request")
