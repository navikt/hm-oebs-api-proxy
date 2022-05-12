package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.lagerstatus.KommuneOppslag
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao
import no.nav.hjelpemidler.service.oebsdatabase.LagerDao

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val kommuneOppslag = KommuneOppslag()
private val brukerpassDao = BrukerpassDao()
private val lagerDao = LagerDao(kommuneOppslag)

fun Route.felles() {
    authenticate("tokenX", "aad") {
        get("/brukerpass") {
            // Extract FNR to lookup from idporten logon
            val fnr = call.getTokenInfo()["pid"]?.asText() ?: error("Could not find 'pid' claim in token")

            if (Configuration.application["APP_PROFILE"]!! != "prod") {
                logg.info("Processing request for /brukerpass (on-behalf-of: $fnr)")
            } else {
                logg.info("Processing request for /brukerpass")
            }

            // Extra sanity check of FNR
            if (!"\\d{11}".toRegex().matches(fnr)) {
                error("invalid fnr in 'pid', does not match regex")
            }

            call.respond(brukerpassDao.brukerpassForFnr(fnr))
        }
    }
    get("/lager/alle-sentraler/{hmsNr}") {
        call.respond(lagerDao.lagerStatus(call.parameters["hmsNr"]!!))
    }

    get("/lager/sentral/{kommunenummer}/{hmsNr}") {
        data class NoResult(
            val error: String,
        )

        val result = lagerDao.lagerStatusSentral(call.parameters["kommunenummer"]!!, call.parameters["hmsNr"]!!)
        if (result != null) {
            call.respond(result)
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                NoResult("no results found for kommunenummer=\"${call.parameters["kommunenummer"]!!}\" and hmsnr=\"${call.parameters["hmsNr"]!!}\"")
            )
        }
    }
}
