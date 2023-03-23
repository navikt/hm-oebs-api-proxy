package no.nav.hjelpemidler

import io.ktor.client.engine.apache.Apache
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.client.oebs.OebsApiClient
import no.nav.hjelpemidler.models.BestillingsOrdreRequest
import no.nav.hjelpemidler.models.Fødselsnummer
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.service.oebsdatabase.Brukernummer
import no.nav.hjelpemidler.service.oebsdatabase.BrukernummerDao
import no.nav.hjelpemidler.service.oebsdatabase.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.oebsdatabase.PersoninformasjonDao
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselDao

private val logg = KotlinLogging.logger {}

private val personinformasjonDao = PersoninformasjonDao()
private val opprettServiceforespørselDao = ServiceforespørselDao()
private val hjelpemiddeloversiktDao = HjelpemiddeloversiktDao()
private val brukernummerDao = BrukernummerDao()
private val oebsApiClient = OebsApiClient(Apache.create())

fun Route.saksbehandling() {
    // Authenticated database proxy requests
    authenticate("aad") {
        post("/opprettOrdre") {
            try {
                val bestilling = call.receive<BestillingsOrdreRequest>()
                val bestillingsResponse = oebsApiClient.opprettOrdre(bestilling)
                logg.info { "Oppretter ordre, saksnummer: ${bestilling.saksnummer}, hjelpemidler: ${bestilling.artikler}" }
                call.respond(HttpStatusCode.Created, bestillingsResponse)
            } catch (e: Exception) {
                logg.error("Noe gikk feil med opprettelse av Ordre", e)
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }
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
            validateFnr(fnr)
            val personinformasjonListe = personinformasjonDao.hentPersoninformasjon(fnr)
            call.respond(personinformasjonListe)
        }

        post("/getBrukernummer") {
            val fødselsnummer = Fødselsnummer(call.receiveText())
            val hentBrukernummer: Brukernummer? = brukernummerDao.hentBrukernummer(fødselsnummer)

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
            val hjelpemiddeloversikt = hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr)
            call.respond(hjelpemiddeloversikt)
        }

        post("/harUtlåntIsokode") {
            val req = call.receive<HarUtlevertIsokodeRequest>()
            val fnr = req.fnr
            val isokode = req.isokode
            validateFnr(fnr)
            val hjelpemiddeloversikt = hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr)
            val harUtlevertIsokode = hjelpemiddeloversikt.find { it.kategoriNummer == isokode } != null
            call.respond(harUtlevertIsokode)
        }
    }
}

private fun validateFnr(fnr: String) {
    if (!"\\d{11}".toRegex().matches(fnr)) {
        error("invalid fnr in 'pid', does not match regex")
    }
}

private data class HarUtlevertIsokodeRequest(
    val fnr: String,
    val isokode: String
)
