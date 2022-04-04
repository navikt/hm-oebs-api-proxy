package no.nav.hjelpemidler

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import mu.KotlinLogging
import no.nav.hjelpemidler.service.oebsdatabase.LagerDao

private val logg = KotlinLogging.logger {}

private val lagerDao = LagerDao()

fun Route.test() {
    get("/test-lager/{hmsNr}") {
        call.respond(lagerDao.lagerStatus(call.parameters["hmsNr"]!!))
    }
}
