package no.nav.hjelpemidler

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
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
            Prometheus.oebsDbAvailable.set(0.0)
            return@get call.respondText("NOT READY", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
        }
        Prometheus.oebsDbAvailable.set(1.0)

        call.respondText("READY", ContentType.Text.Plain)
    }

    get("/metrics") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
        }
    }
}
