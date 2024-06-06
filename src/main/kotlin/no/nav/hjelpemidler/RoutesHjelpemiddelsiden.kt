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

fun Route.hjelpemiddelsiden(database: Database) {
    // Authenticated database proxy requests
    authenticate("tokenX") {
        get("/hjelpemidler-bruker") {
            // Extract FNR to lookup from idporten logon
            val fnr = call.getTokenInfo()["pid"]?.asText() ?: error("Could not find 'pid' claim in token")
            if (isNotProd()) {
                log.info { "Processing request for /hjelpemidler-bruker (on-behalf-of: $fnr)" }
            } else {
                log.info { "Processing request for /hjelpemidler-bruker" }
            }

            // Extra sanity check of FNR
            if (!"\\d{11}".toRegex().matches(fnr)) {
                error("invalid fnr in 'pid', does not match regex")
            }

            val hjelpemiddeloversikt = database.transaction {
                hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr)
            }

            call.respond(hjelpemiddeloversikt)
        }
    }

    // Authenticated database proxy requests
    authenticate("aad") {
        get("/get-title-for-hmsnr/{hmsNr}") {
            val result = database.transaction {
                tittelForHmsnrDao.hentTittelForHmsnr(call.parameters["hmsNr"]!!)
            }
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, """{"error": "product or accessory not found"}""")
                return@get
            }
            call.respond(result)
        }

        post("/get-title-for-hmsnrs") {
            val hmsnrs = call.receive<Array<String>>().toList().toSet()
            val tittelForHmsnrs = database.transaction {
                tittelForHmsnrDao.hentTittelForHmsnrs(hmsnrs)
            }
            call.respond(tittelForHmsnrs)
        }
    }
}
