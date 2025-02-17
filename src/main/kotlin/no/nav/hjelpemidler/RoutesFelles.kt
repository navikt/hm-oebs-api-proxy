package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.service.NorgService

private val log = KotlinLogging.logger {}

fun Route.felles(database: Database, norgService: NorgService) {
    authenticate("aad") {
        post("/hent-brukerpass") {
            data class FnrDto(
                val fnr: Fødselsnummer,
            )

            val fnr = call.receive<FnrDto>().fnr
            val brukerpass = database.transaction { brukerpassDao.brukerpassForFnr(fnr.value) }
            call.respond(brukerpass)
        }
    }

    authenticate("tokenX", "aad") {
        get("/brukerpass") {
            val fnr = call.pid

            if (!Environment.current.isProd) {
                log.info { "Processing request for /brukerpass (on-behalf-of: $fnr)" }
            } else {
                log.info { "Processing request for /brukerpass" }
            }

            val brukerpass = database.transaction { brukerpassDao.brukerpassForFnr(fnr.value) }

            call.respond(brukerpass)
        }

        get("/lager/alle-sentraler/{hmsNr}") {
            val lagerstatus = database.transaction { lagerDao.hentLagerstatus(call.parameters["hmsNr"]!!) }
            call.respond(lagerstatus)
        }

        get("/lager/sentral/{kommunenummer}/{hmsNr}") {
            data class NoResult(
                val error: String,
            )

            val kommunenummer = call.parameters["kommunenummer"]!!
            val enhetNavn = norgService.hentEnhetNavn(kommunenummer) ?: error("Fant ikke enhetNavn for kommunenummer $kommunenummer")

            val lagerstatus = database.transaction {
                lagerDao.hentLagerstatusForSentral(enhetNavn, call.parameters["hmsNr"]!!)
            }
            if (lagerstatus != null) {
                call.respond(lagerstatus)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    NoResult("no results found for kommunenummer=\"${call.parameters["kommunenummer"]!!}\" and hmsnr=\"${call.parameters["hmsNr"]!!}\""),
                )
            }
        }

        post("/lager/sentral/{kommunenummer}") {
            data class HmsnrsDTO(
                val hmsnrs: List<String>,
            )
            val hmsnrs = call.receive<HmsnrsDTO>().hmsnrs
            val kommunenummer = call.parameters["kommunenummer"]!!

            data class NoResult(
                val error: String,
            )

            val enhetNavn = norgService.hentEnhetNavn(kommunenummer) ?: error("Fant ikke enhetNavn for kommunenummer $kommunenummer")

            val lagerstatus = database.transaction {
                lagerDao.hentLagerstatusForSentral(enhetNavn, hmsnrs)
            }
            if (lagerstatus != null) {
                call.respond(lagerstatus)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    NoResult("no results found for kommunenummer=\"${kommunenummer}\" and hmsnrs=\"${hmsnrs}\""),
                )
            }
        }
    }
}
