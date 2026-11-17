package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.ktor.receiveFødselsnummer
import no.nav.hjelpemidler.server.ktor.security.AuthenticationProviderName

private val log = KotlinLogging.logger {}

fun Route.kommuneApi() {
    val database: Database by application.dependencies

    // Authenticated database proxy requests
    authenticate(AuthenticationProviderName.ENTRA_ID) {
        route("/kommune-api") {
            post("/utlansoversikt") {
                val fnr = call.receiveFødselsnummer()
                val hjelpemiddeloversikt = database.transaction {
                    hjelpemiddeloversiktDao.hentHjelpemiddeloversiktForKommuneApi(fnr.value)
                }
                call.respond(hjelpemiddeloversikt)
            }
        }
    }
}
