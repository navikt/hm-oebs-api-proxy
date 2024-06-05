package no.nav.hjelpemidler.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.util.concurrent.atomic.AtomicInteger

object Prometheus {
    val registry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val oebsDbAvailable: AtomicInteger = registry.gauge(
        "hm_oebs_api_proxy_oebs_db_available",
        AtomicInteger(0),
    )!!
}
