package no.nav.hjelpemidler

import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.testcontainers.containers.OracleContainer

internal object OracleTestHelper {

    val instance by lazy {
        OracleContainer("gvenzl/oracle-xe").apply {
            start()
        }
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", instance.host)
            addDataSourceProperty("portNumber", instance.oraclePort)
            addDataSourceProperty("databaseName", instance.databaseName)
            addDataSourceProperty("user", instance.username)
            addDataSourceProperty("password", instance.password)
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }.also { sessionOf(it).run(queryOf("DROP ROLE IF EXISTS cloudsqliamuser").asExecute) }
            .also { sessionOf(it).run(queryOf("CREATE ROLE cloudsqliamuser").asExecute) }
            .also { sessionOf(it).run(queryOf("CREATE TABLE XXRTV_DIGIHOT_OEBS_ADR_FNR_V").asExecute) }
    }

    fun withDb(block: () -> Unit) {
        dataSource.run { block() }
    }
}
