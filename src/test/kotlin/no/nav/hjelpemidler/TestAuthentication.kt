package no.nav.hjelpemidler

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal

fun Application.installTestAuthentication() {
    install(Authentication) {
        provider("tokenX") {
            authenticate {
                it.principal(UserIdPrincipal("test"))
            }
        }
        provider("aad") {
            authenticate {
                it.principal(UserIdPrincipal("test"))
            }
        }
    }
}
