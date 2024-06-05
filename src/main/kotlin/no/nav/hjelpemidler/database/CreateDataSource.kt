package no.nav.hjelpemidler.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

fun createDataSource(block: HikariConfig.() -> Unit): HikariDataSource {
    val configuration = HikariConfig().apply(block)
    log.info { "Oppretter datasource, jdbcUrl: '${configuration.jdbcUrl}'" }
    return HikariDataSource(configuration)
}
