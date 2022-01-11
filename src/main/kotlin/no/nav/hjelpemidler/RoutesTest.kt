package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import mu.KotlinLogging
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

private val brukerpassDao = BrukerpassDao()

fun Route.test() {
    get("/test-brukerpass") {
        val results = mutableListOf(
            BrukerpassResult(
                hasBrukerpass = brukerpassDao.brukerpassForFnr("15084300133") ?: false,
            ),
            BrukerpassResult(
                hasBrukerpass = brukerpassDao.brukerpassForFnr("10127622634") ?: false,
            ),
        )
        call.respond(results)
    }
}

private data class BrukerpassResult(
    val hasBrukerpass: Boolean,
)
