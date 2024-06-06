package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.database.Database

private val log = KotlinLogging.logger {}

fun Route.felles(database: Database) {
    authenticate("aad") {
        post("/hent-brukerpass") {
            val fnr = call.receive<FnrDto>().fnr

            // Extra sanity check of FNR
            if (!"\\d{11}".toRegex().matches(fnr)) {
                error("invalid fnr from body, does not match regex")
            }

            val brukerpass = database.transaction { brukerpassDao.brukerpassForFnr(fnr) }

            call.respond(brukerpass)
        }
    }

    authenticate("tokenX", "aad") {
        get("/brukerpass") {
            // Extract FNR to lookup from idporten logon
            val fnr = call.getTokenInfo()["pid"]?.asText() ?: error("Could not find 'pid' claim in token")

            if (isNotProd()) {
                log.info { "Processing request for /brukerpass (on-behalf-of: $fnr)" }
            } else {
                log.info { "Processing request for /brukerpass" }
            }

            // Extra sanity check of FNR
            if (!"\\d{11}".toRegex().matches(fnr)) {
                error("invalid fnr in 'pid', does not match regex")
            }

            val brukerpass = database.transaction { brukerpassDao.brukerpassForFnr(fnr) }

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

            val lagerstatus = database.transaction {
                lagerDao.hentLagerstatusForSentral(call.parameters["kommunenummer"]!!, call.parameters["hmsNr"]!!)
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
    }
}

private data class FnrDto(
    val fnr: String,
)
