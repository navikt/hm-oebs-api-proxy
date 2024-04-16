package no.nav.hjelpemidler.database

import no.nav.hjelpemidler.Configuration

object Configuration {
    val dataSource by lazy {
        createDataSource {
            jdbcUrl = Configuration.OEBS_DB_JDBC_URL_T2
            username = Configuration.OEBS_DB_USERNAME
            password = Configuration.OEBS_DB_PASSWORD
            driverClassName = "oracle.jdbc.OracleDriver"
            connectionTimeout = 10000
            idleTimeout = 10001
            maxLifetime = 30001
            maximumPoolSize = 10
            minimumIdle = 1
        }
    }
}
