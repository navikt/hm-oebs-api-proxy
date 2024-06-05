package no.nav.hjelpemidler

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.hjelpemidler.database.Configuration
import no.nav.hjelpemidler.metrics.Prometheus

fun Route.internal() {
    get("/isalive") {
        // Let's check if the datasource has been closed
        if (Configuration.dataSource.isClosed) {
            return@get call.respondText("NOT ALIVE", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        }
        call.respondText("ALIVE", ContentType.Text.Plain)
    }

    get("/isready") {
        // Let's check if the datasource is still valid and working
        val dbConnectionValid = Configuration.dataSource.connection.use {
            it.isValid(20)
        }
        if (!dbConnectionValid) {
            Prometheus.oebsDbAvailable.set(0)
            return@get call.respondText("NOT READY", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        }
        Prometheus.oebsDbAvailable.set(1)

        call.respondText("READY", ContentType.Text.Plain)
    }

    get("/metrics") {
        call.respond(Prometheus.registry.scrape())
    }
}
