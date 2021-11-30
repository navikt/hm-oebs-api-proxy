package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.service.oebsdatabase.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.oebsdatabase.TittelForHmsnrDao

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val hjelpemiddeloversiktDao = HjelpemiddeloversiktDao()
private val tittleForHmsnrDao = TittelForHmsnrDao()

fun Route.hjelpemiddelsiden() {
    // Authenticated database proxy requests
    authenticate("tokenX") {
        get("/hjelpemidler-bruker") {
            // Extract FNR to lookup from idporten logon
            val fnr = call.getTokenInfo()["pid"]?.asText() ?: error("Could not find 'pid' claim in token")
            if (Configuration.application["APP_PROFILE"]!! != "prod") {
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
