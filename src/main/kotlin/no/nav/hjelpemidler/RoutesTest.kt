package no.nav.hjelpemidler

import io.ktor.server.routing.Route
import mu.KotlinLogging

private val logg = KotlinLogging.logger {}

// private val lagerDao = LagerDao()

fun Route.test() {
    /*
    get("/test-lager/{hmsNr}") {
        call.respond(lagerDao.lagerStatus(call.parameters["hmsNr"]!!))
    }
    */
}
