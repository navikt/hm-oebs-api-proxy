package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val brukerpassDao = BrukerpassDao()

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

            val bp = brukerpassDao.brukerpassForFnr(fnr)

            data class Result(
                val fantBruker: Boolean,
                val brukerpass: Boolean,
            )

            call.respond(Result(bp != null, bp ?: false))
        }
    }
}
