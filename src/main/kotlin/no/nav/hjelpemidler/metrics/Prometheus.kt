package no.nav.hjelpemidler.metrics

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge

internal object Prometheus {
    val collectorRegistry = CollectorRegistry.defaultRegistry

    val oebsDbAvailable = Gauge
        .build()
        .name("HM_OEBS_API_PROXY_oebs_db_available")
        .help("OEBS oracle-database tilgjengelig")
        .register(collectorRegistry)
}
