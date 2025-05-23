package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.metrics.Prometheus

fun Route.internal(database: Database) {
    get("/isalive") {
        // Let's check if the database has been closed
        if (database.isClosed) {
            call.respondText("NOT ALIVE", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        } else {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
    }

    get("/isready") {
        // Let's check if the database is still valid and working
        if (!database.isValid()) {
            Prometheus.oebsDbAvailable.set(0)
            call.respondText("NOT READY", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        } else {
            Prometheus.oebsDbAvailable.set(1)
            call.respondText("READY", ContentType.Text.Plain)
        }
    }

    get("/metrics") {
        call.respond(Prometheus.registry.scrape())
    }

    post("/internal/ping-oebs-rest-api") {
        val oebsApiClient = OebsApiClient(CIO.create())
        val log = KotlinLogging.logger {}
        log.info { "Kaller OEBS rest-api ping" }
        val success = oebsApiClient.ping()
        log.info { "Etter kall mot OEBS rest-api ping" }

        if (success) {
            call.respond(HttpStatusCode.OK, "OEBS ok.")
        } else {
            call.respond(HttpStatusCode.InternalServerError, "OEBS did not return expected result.")
        }
    }
}
