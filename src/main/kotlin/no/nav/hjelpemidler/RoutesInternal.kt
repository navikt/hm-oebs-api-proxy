package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.metrics.Prometheus
import no.nav.hjelpemidler.models.Brukernummer
import no.nav.hjelpemidler.models.Brukerpass
import no.nav.hjelpemidler.models.TittelForHmsNr
import no.nav.hjelpemidler.models.Utlån
import no.nav.hjelpemidler.service.Lagerstatus

fun Route.internal(database: Database) {
    get("/isalive") {
        // Let's check if the database has been closed
        if (database.isClosed) {
            call.respondText("NOT ALIVE", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        } else {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
    }

    get("/isready") {
        // Let's check if the database is still valid and working
        if (!database.isValid()) {
            Prometheus.oebsDbAvailable.set(0)
            call.respondText("NOT READY", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        } else {
            Prometheus.oebsDbAvailable.set(1)
            call.respondText("READY", ContentType.Text.Plain)
        }
    }

    get("/metrics") {
        call.respond(Prometheus.registry.scrape())
    }

    post("/internal/ping-oebs-rest-api") {
        val log = KotlinLogging.logger {}
        val oebsApiClient = OebsApiClient(CIO.create())
        log.info { "Kaller OEBS rest-api ping" }
        val success = oebsApiClient.ping()
        log.info { "Etter kall mot OEBS rest-api ping" }

        if (success) {
            call.respond(HttpStatusCode.OK, "OEBS ok.")
        } else {
            call.respond(HttpStatusCode.InternalServerError, "OEBS did not return expected result.")
        }
    }

    post("/internal/test-oebs-dblink-view") {
        data class Request(
            val hmsnr: String?,
            val fnr: String?,
            val enhetsNavn: String?,
        )
        data class Response(
            val utlån: List<Utlån>,
            val brukernummer: Brukernummer?,
            val tittelForHmsnr: TittelForHmsNr?,
            val brukerpass: Brukerpass?,
            val lagerstatus: List<Lagerstatus>,
        )

        val req = call.receive<Request>()

        val hmsnr = req.hmsnr ?: "014112"
        val fnr = req.fnr ?: "26848497710"
        val enhetsNavn = req.enhetsNavn

        call.respond(
            database.transaction {
                Response(
                    hjelpemiddeloversiktDao.utlånPåArtnr(hmsnr),
                    brukernummerDao.hentBrukernummer(Fødselsnummer(fnr)),
                    tittelForHmsnrDao.hentTittelForHmsnr(hmsnr),
                    brukerpassDao.brukerpassForFnr(fnr),
                    if (enhetsNavn == null) {
                        lagerDao.hentLagerstatus(hmsnr)
                    } else {
                        listOfNotNull(lagerDao.hentLagerstatusForSentral(hmsnr, enhetsNavn))
                    },
                )
            },
        )
    }
}
