package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val brukerpassDao = BrukerpassDao()

fun Route.test() {
    get("/test-brukerpass") {
        val results = mutableListOf(
            BrukerpassResult(
                fnr = "15084300133",
                hasBrukerpass = brukerpassDao.brukerpassForFnr("15084300133") ?: false,
            ),
            BrukerpassResult(
                fnr = "10127622634",
                hasBrukerpass = brukerpassDao.brukerpassForFnr("10127622634") ?: false,
            ),
            BrukerpassResult(
                fnr = Configuration.application["TESTSECRET"]!!.trim(),
                hasBrukerpass = brukerpassDao.brukerpassForFnr(Configuration.application["TESTSECRET"]!!.trim()) ?: false,
            ),
        )
        call.respond(results)
    }
}

private data class BrukerpassResult(
    val fnr: String,
    val hasBrukerpass: Boolean,
)
