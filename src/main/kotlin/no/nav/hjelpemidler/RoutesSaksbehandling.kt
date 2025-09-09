package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.ktor.receiveFødselsnummer
import no.nav.hjelpemidler.models.BestillingsordreRequest
import no.nav.hjelpemidler.models.Brukernummer
import no.nav.hjelpemidler.models.Personinformasjon
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.Utlån
import kotlin.String

private val log = KotlinLogging.logger {}

private val oebsApiClient = OebsApiClient(CIO.create())

fun Route.saksbehandling(database: Database) {
    // Authenticated database proxy requests
    authenticate("aad") {
        post("/opprettOrdre") {
            try {
                val bestilling = call.receive<BestillingsordreRequest>()
                val response = oebsApiClient.opprettOrdre(bestilling)
                log.info { "Oppretter ordre, saksnummer: ${bestilling.saksnummer}, hjelpemidler: ${bestilling.artikler}, ferdigstillOrdre: ${bestilling.ferdigstillOrdre}" }
                call.respond(HttpStatusCode.Created, response)
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med opprettelse av ordre" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        post("/opprettSF") {
            try {
                val serviceforespørsel = call.receive<Serviceforespørsel>()
                database.transaction {
                    val personinformasjon = personinformasjonDao.hentPersoninformasjon(serviceforespørsel.fødselsnummer)
                        .filter(Personinformasjon::aktiv)
                        .filterNot { it.bostedsadresse.adresse.isBlank() || it.bostedsadresse.adresse == "." }
                    serviceforespørselDao.opprettServiceforespørsel(
                        when {
                            personinformasjon.isEmpty() -> {
                                log.warn { "Bruker har ingen aktive adresser i OEBS, tar ikke med kostnadslinjer i serviceforespørsel" }
                                serviceforespørsel.copy(artikler = null)
                            }

                            else -> serviceforespørsel
                        },
                    )
                }
                log.info { "Serviceforespørsel for sakId: ${serviceforespørsel.referansenummer} opprettet, hjelpemidler: ${serviceforespørsel.artikler}" }
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med opprettelse av serviceforespørsel" }
                throw e
            }
        }

        post("/getLeveringsaddresse") {
            val fnr = call.receiveFødselsnummer()
            val personinformasjon = database.transaction { personinformasjonDao.hentPersoninformasjon(fnr.value) }
            call.respond(personinformasjon)
        }

        post("/getBrukernummer") {
            val fnr = call.receiveFødselsnummer()
            val hentBrukernummer: Brukernummer? = database.transaction {
                brukernummerDao.hentBrukernummer(fnr)
            }

            when (hentBrukernummer) {
                null -> call.respond(status = HttpStatusCode.NotFound, "Bruker ikke funnet i OEBS")
                else -> call.respond(hentBrukernummer)
            }
        }

        post("/getHjelpemiddelOversikt") {
            val fnr = call.receiveFødselsnummer()
            val hjelpemiddeloversikt = database.transaction {
                hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr.value)
            }
            call.respond(hjelpemiddeloversikt)
        }

        post("/harUtlantIsokode") {
            try {
                data class HarUtlåntIsokodeRequest(
                    val fnr: Fødselsnummer,
                    val isokode: String,
                )

                val request = call.receive<HarUtlåntIsokodeRequest>()
                val harUtlåntIsokode = database.transaction {
                    hjelpemiddeloversiktDao.utlånPåIsokode(request.fnr.value, request.isokode)
                }.isNotEmpty()
                call.respond(harUtlåntIsokode)
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med sjekk av utlån på isokode" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        post("/utlanSerienrArtnr") {
            try {
                data class UtlånPåArtnrOgSerienrRequest(
                    val artnr: String,
                    val serienr: String,
                )

                val req = call.receive<UtlånPåArtnrOgSerienrRequest>()
                val artnr = req.artnr
                val serienr = req.serienr

                data class UtlånPåArtnrOgSerienrResponse(
                    val utlån: Utlån?,
                )

                // val utlån = database.transaction { hjelpemiddeloversiktDao.utlånPåArtnrOgSerienr(artnr, serienr) }

                val utlån = if (Environment.current.isDev) {
                    // TODO: Fjern hardkodet utlån
                    Utlån(
                        fnr = "12507838047",
                        artnr = artnr,
                        serienr = serienr,
                        opprettetDato = "2025-08-01",
                        utlånsDato = "2025-09-01",
                    )
                } else {
                    database.transaction { hjelpemiddeloversiktDao.utlånPåArtnrOgSerienr(artnr, serienr) }
                }

                call.respond(UtlånPåArtnrOgSerienrResponse(utlån))
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med sjekk av utlån på artnr og serienr" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        if (!Environment.current.isProd) {
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
