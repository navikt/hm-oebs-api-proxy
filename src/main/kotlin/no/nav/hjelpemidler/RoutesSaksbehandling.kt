package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.hjelpemidler.client.oebs.OebsApiClient
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.models.BestillingsordreRequest
import no.nav.hjelpemidler.models.Fødselsnummer
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.Utlån
import no.nav.hjelpemidler.service.oebsdatabase.Brukernummer

private val log = KotlinLogging.logger {}

private val oebsApiClient = OebsApiClient(CIO.create())

fun Route.saksbehandling(database: Database) {
    // Authenticated database proxy requests
    authenticate("aad") {
        post("/opprettOrdre") {
            try {
                val bestilling = call.receive<BestillingsordreRequest>()
                val response = oebsApiClient.opprettOrdre(bestilling)
                log.info { "Oppretter ordre, saksnummer: ${bestilling.saksnummer}, hjelpemidler: ${bestilling.artikler}" }
                call.respond(HttpStatusCode.Created, response)
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med opprettelse av ordre" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        post("/opprettSF") {
            try {
                val serviceforespørsel = call.receive<Serviceforespørsel>()
                database.transaction { serviceforespørselDao.opprettServiceforespørsel(serviceforespørsel) }
                log.info { "Serviceforespørsel for sakId: ${serviceforespørsel.referansenummer} opprettet, hjelpemidler: ${serviceforespørsel.artikler}" }
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med opprettelse av SF" }
                throw e
            }
        }

        post("/getLeveringsaddresse") {
            val fnr = call.receiveText()
            // Extra sanity check of FNR
            validateFnr(fnr)
            val personinformasjon = database.transaction { personinformasjonDao.hentPersoninformasjon(fnr) }
            call.respond(personinformasjon)
        }

        post("/getBrukernummer") {
            val fødselsnummer = Fødselsnummer(call.receiveText())
            val hentBrukernummer: Brukernummer? = database.transaction {
                brukernummerDao.hentBrukernummer(fødselsnummer)
            }

            when (hentBrukernummer) {
                null -> {
                    call.respond(status = HttpStatusCode.NotFound, "Bruker ikke funnet i OEBS")
                }

                else -> {
                    call.respond(hentBrukernummer)
                }
            }
        }

        post("/getHjelpemiddelOversikt") {
            val fnr = call.receiveText()
            // Extra sanity check of FNR
            validateFnr(fnr)
            val hjelpemiddeloversikt = database.transaction { hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr) }
            call.respond(hjelpemiddeloversikt)
        }

        post("/harUtlantIsokode") {
            try {
                val req = call.receive<HarUtlåntIsokodeRequest>()
                val fnr = req.fnr
                val isokode = req.isokode
                validateFnr(fnr)
                val harUtlåntIsokode = database.transaction {
                    hjelpemiddeloversiktDao.utlånPåIsokode(fnr, isokode)
                }.isNotEmpty()
                call.respond(harUtlåntIsokode)
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med sjekk av utlån på isokode" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        post("/utlanSerienrArtnr") {
            try {
                val req = call.receive<UtlånPåArtnrOgSerienrRequest>()
                val artnr = req.artnr
                val serienr = req.serienr
                val utlån = database.transaction { hjelpemiddeloversiktDao.utlånPåArtnrOgSerienr(artnr, serienr) }
                call.respond(UtlånPåArtnrOgSerienrResponse(utlån))
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med sjekk av utlån på artnr og serienr" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        if (isNotProd()) {
            post("/utlanArtnr") {
                try {
                    val artnr = call.receiveText()
                    val utlån = database.transaction { hjelpemiddeloversiktDao.utlånPåArtnr(artnr) }
                    call.respond(utlån)
                } catch (e: Exception) {
                    log.error(e) { "Noe gikk feil med sjekk av utlån på artnr" }
                    call.respond(HttpStatusCode.InternalServerError, e)
                }
            }
        }
    }
}

private fun validateFnr(fnr: String) {
    if (!"\\d{11}".toRegex().matches(fnr)) {
        error("invalid fnr in 'pid', does not match regex")
    }
}

private data class HarUtlåntIsokodeRequest(
    val fnr: String,
    val isokode: String,
)

private data class UtlånPåArtnrOgSerienrRequest(
    val artnr: String,
    val serienr: String,
)

private data class UtlånPåArtnrOgSerienrResponse(
    val utlån: Utlån?,
)
