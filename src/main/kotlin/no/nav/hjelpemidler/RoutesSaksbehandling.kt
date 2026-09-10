package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.ktor.receiveFødselsnummer
import no.nav.hjelpemidler.models.BestillingsordreRequest
import no.nav.hjelpemidler.models.Personinformasjon
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.ServiceforespørselRequest
import no.nav.hjelpemidler.models.Utlån
import no.nav.hjelpemidler.models.UtlånMedSerienr

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
                val sfRequest = call.receive<ServiceforespørselRequest>()
                database.transaction {
                    val personinformasjon = personinformasjonDao.hentPersoninformasjon(sfRequest.fødselsnummer)
                        .filter(Personinformasjon::aktiv)
                        .filterNot { it.bostedsadresse.adresse.isBlank() || it.bostedsadresse.adresse == "." }

                    val sf = when {
                        personinformasjon.isEmpty() -> {
                            log.warn { "Bruker har ingen aktive adresser i OEBS, tar ikke med kostnadslinjer i serviceforespørsel" }
                            sfRequest.copy(artikler = null)
                        }

                        else -> sfRequest
                    }

                    val serviceforespørsel = Serviceforespørsel(
                        fødselsnummer = sf.fødselsnummer,
                        navn = sf.navn,
                        stønadsklasse = sf.stønadsklasse,
                        resultat = sf.resultat,
                        referansenummer = sf.referansenummer,
                        problemsammendrag = sf.problemsammendrag,
                        forsendelsesinfo = sf.forsendelsesinfo?.trim()?.removeSuffix("."),
                        artikler = sf.artikler,
                        notat = sf.notat?.let { Serviceforespørsel.Notat(notatInfo = it) },
                    )

                    serviceforespørselDao.opprettServiceforespørsel(
                        serviceforespørsel,
                    )
                }
                log.info { "Serviceforespørsel for sakId: ${sfRequest.referansenummer} opprettet, hjelpemidler: ${sfRequest.artikler}" }
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
            val brukernummer = database.transaction { brukernummerDao.hentBrukernummer(fnr) }
            if (brukernummer == null) {
                call.respond(status = HttpStatusCode.NotFound, "Bruker ikke funnet i OeBS")
            } else {
                call.respond(brukernummer)
            }
        }

        post("/getBrukernumre") {
            val fnr = call.receive<Set<Fødselsnummer>>()
            val brukernumre = database.transaction { brukernummerDao.hentBrukernumre(fnr) }
            call.respond(brukernumre)
        }

        get("/getFodselsnummer/{brukernummer}") {
            val brukernummer = call.parameters["brukernummer"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Brukernr mangler",
            )
            val fnr = database.transaction {
                brukernummerDao.hentFødselsnummer(brukernummer)
            }
            call.respond(fnr)
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

                data class UtlånResponse(
                    val utlån: UtlånMedSerienr?,
                )

                val req = call.receive<UtlånPåArtnrOgSerienrRequest>()
                val artnr = req.artnr
                val serienr = req.serienr

                val utlån = database.transaction { hjelpemiddeloversiktDao.utlånPåArtnrOgSerienr(artnr, serienr) }
                if (Environment.current.isDev) {
                    log.info { "utlån: $utlån" }
                }

                call.respond(UtlånResponse(utlån))
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med sjekk av utlån på artnr og serienr" }
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }

        post("/utlanBrukernrArtnr") {
            try {
                data class UtlånPåArtnrOgBrukernrRequest(
                    val artnr: String,
                    val brukernr: String,
                )

                data class UtlånResponse(
                    val utlån: List<Utlån>,
                )

                val req = call.receive<UtlånPåArtnrOgBrukernrRequest>()
                val artnr = req.artnr
                val brukernr = req.brukernr

                val fnr = database.transaction { brukernummerDao.hentFødselsnummer(brukernr) }
                if (Environment.current.isDev) {
                    log.info { "Fødselsnr: $fnr" }
                }

                val utlån = database.transaction { hjelpemiddeloversiktDao.utlånPåArtnrOgFødselsnr(artnr, fnr.value) }
                if (Environment.current.isDev) {
                    log.info { "utlån: $utlån" }
                }

                call.respond(UtlånResponse(utlån))
            } catch (e: Exception) {
                log.error(e) { "Noe gikk feil med sjekk av utlån på artnr og brukernr" }
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
