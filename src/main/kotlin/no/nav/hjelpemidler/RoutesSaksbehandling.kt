package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import mu.KotlinLogging
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

fun Route.saksbehandling() {
    // Authenticated database proxy requests
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
            val fnr = Fødselsnummer(call.receiveText())
            val hjelpemiddeloversikt = hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr)
            call.respond(hjelpemiddeloversikt)
        }
    }
}

private fun validateFnr(fnr: String) {
    if (!"\\d{11}".toRegex().matches(fnr)) {
        error("invalid fnr in 'pid', does not match regex")
    }
}
