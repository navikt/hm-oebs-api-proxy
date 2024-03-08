package no.nav.hjelpemidler.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import no.nav.hjelpemidler.Configuration

object Configuration {
    private val log = KotlinLogging.logger {}

    val dataSource by lazy {
        val configuration = HikariConfig().apply {
            jdbcUrl = Configuration.OEBS_DB_JDBC_URL
            username = Configuration.OEBS_DB_USERNAME
            password = Configuration.OEBS_DB_PASSWORD
            driverClassName = "oracle.jdbc.OracleDriver"
            connectionTimeout = 1000
            idleTimeout = 10001
            maxLifetime = 30001
            maximumPoolSize = 10
            minimumIdle = 1
        }
        log.info { "Oppretter datasource, jdbcUrl: '${configuration.jdbcUrl}'" }
        HikariDataSource(configuration)
    }
}
