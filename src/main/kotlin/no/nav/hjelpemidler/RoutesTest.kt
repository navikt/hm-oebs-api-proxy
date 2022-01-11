package no.nav.hjelpemidler

import io.ktor.routing.Route
import mu.KotlinLogging

private val logg = KotlinLogging.logger {}

// private val brukerpassDao = BrukerpassDao()

fun Route.test() {
/*
    get("/test-brukerpass") {
        private data class BrukerpassResult(
            val fnr: String,
            val hasBrukerpass: Boolean,
        )

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
 */
}
