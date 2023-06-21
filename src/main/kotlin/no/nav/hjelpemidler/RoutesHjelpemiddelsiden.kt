package no.nav.hjelpemidler

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.isNotProd
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao
import no.nav.hjelpemidler.service.oebsdatabase.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.oebsdatabase.TittelForHmsnrDao

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val hjelpemiddeloversiktDao = HjelpemiddeloversiktDao()
private val tittleForHmsnrDao = TittelForHmsnrDao()
private val brukerpassDao = BrukerpassDao()

fun Route.hjelpemiddelsiden() {
    // Authenticated database proxy requests
    authenticate("tokenX") {
        get("/hjelpemidler-bruker") {
            // Extract FNR to lookup from idporten logon
            val fnr = call.getTokenInfo()["pid"]?.asText() ?: error("Could not find 'pid' claim in token")
            if (isNotProd()) {
                logg.info("Processing request for /hjelpemidler-bruker (on-behalf-of: $fnr)")
            } else {
                logg.info("Processing request for /hjelpemidler-bruker")
            }

            // Extra sanity check of FNR
            if (!"\\d{11}".toRegex().matches(fnr)) {
                error("invalid fnr in 'pid', does not match regex")
            }

            call.respond(hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr))
        }
    }

    // Authenticated database proxy requests
    authenticate("aad") {
        get("/get-title-for-hmsnr/{hmsNr}") {
            val result = tittleForHmsnrDao.hentTittelForHmsnr(call.parameters["hmsNr"]!!)
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, """{"error": "product or accessory not found"}""")
                return@get
            }
            call.respond(result)
        }

        post("/get-title-for-hmsnrs") {
            val hmsnrs = call.receive<Array<String>>().toList().toSet()
            call.respond(tittleForHmsnrDao.hentTittelForHmsnrs(hmsnrs))
        }
    }
}
