package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.ktor.receiveFødselsnummer

private val log = KotlinLogging.logger {}

private val oebsApiClient = OebsApiClient(CIO.create())

fun Route.kommuneApi(database: Database) {
    // Authenticated database proxy requests
    authenticate("aad") {
        route("/kommune-api") {
            post("/getHjelpemiddelOversikt") {
                val fnr = call.receiveFødselsnummer()
                val hjelpemiddeloversikt = database.transaction {
                    hjelpemiddeloversiktDao.hentHjelpemiddeloversiktForKommuneApi(fnr.value)
                }
                call.respond(hjelpemiddeloversikt)
            }
        }
    }
}
