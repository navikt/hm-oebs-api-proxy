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

private val log = KotlinLogging.logger {}

fun Route.hjelpemiddelsiden(database: Database) {
    // Authenticated database proxy requests
    authenticate("tokenX") {
        get("/hjelpemidler-bruker") {
            val fnr = call.pid
            if (!Environment.current.isProd) {
                log.info { "Processing request for /hjelpemidler-bruker (on-behalf-of: $fnr)" }
            } else {
                log.info { "Processing request for /hjelpemidler-bruker" }
            }

            val hjelpemiddeloversikt = database.transaction {
                hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr.value)
            }

            call.respond(hjelpemiddeloversikt)
        }
    }

    // Authenticated database proxy requests
    authenticate("aad") {
        get("/get-title-for-hmsnr/{hmsnr}") {
            val result = database.transaction {
                tittelForHmsnrDao.hentTittelForHmsnr(call.parameters["hmsnr"]!!)
            }
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "product or accessory not found"))
                return@get
            }
            call.respond(result)
        }

        post("/get-title-for-hmsnrs") {
            val hmsnrs = call.receive<Set<String>>()
            val tittelForHmsnrs = database.transaction {
                tittelForHmsnrDao.hentTittelForHmsnrs(hmsnrs)
            }
            call.respond(tittelForHmsnrs)
        }
    }
}
