package no.nav.hjelpemidler.configuration

import com.natpryce.konfig.*
import com.zaxxer.hikari.HikariDataSource

internal object Configuration {

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
        "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
        else -> {
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
        }
    }

    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to "prod",

            "HM_OEBS_API_PROXY_DB_NAME" to "oebsp",
            // "HM_OEBS_API_PROXY_DB_URL" to "jdbc:oracle:thin:@dm09db08.adeo.no:1521/oebsp",    // P-env
            "HM_OEBS_API_PROXY_DB_URL" to System.getenv ("HM_OEBS_API_PROXY_DB_URL_P"),
            "HM_OEBS_API_PROXY_DB_USR" to System.getenv ("HM_OEBS_API_PROXY_DB_USR_P"),
            "HM_OEBS_API_PROXY_DB_PW" to System.getenv ("HM_OEBS_API_PROXY_DB_PW_P"),

            "HJELPEMIDDELDATABASEN_API" to "https://hm-produktinfo-api.intern.nav.no/api",
        )
    )

    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to "dev",

             "HM_OEBS_API_PROXY_DB_NAME" to "oebst1",
             "HM_OEBS_API_PROXY_DB_URL" to System.getenv ("HM_OEBS_API_PROXY_DB_URL_T1"),
             "HM_OEBS_API_PROXY_DB_USR" to System.getenv ("HM_OEBS_API_PROXY_DB_USR_T1"),
             "HM_OEBS_API_PROXY_DB_PW" to System.getenv ("HM_OEBS_API_PROXY_DB_PW_T1"),

//            "HM_OEBS_API_PROXY_DB_NAME" to "oebsq1",
//            "HM_OEBS_API_PROXY_DB_URL" to System.getenv ("HM_OEBS_API_PROXY_DB_URL_Q1"),
//            "HM_OEBS_API_PROXY_DB_USR" to System.getenv ("HM_OEBS_API_PROXY_DB_USR_Q1"),
//            "HM_OEBS_API_PROXY_DB_PW" to System.getenv ("HM_OEBS_API_PROXY_DB_PW_Q1"),

            // "HJELPEMIDDELDATABASEN_API" to "https://hm-produktinfo-api-poc.nais.preprod.local/api",
            "HJELPEMIDDELDATABASEN_API" to "https://hm-produktinfo-api.dev.intern.nav.no/api",
        )
    )

    private val localProperties = ConfigurationMap (
        mapOf(
            "application.profile" to "local",

            "HM_OEBS_API_PROXY_DB_URL" to "abc",
            "HM_OEBS_API_PROXY_DB_USR" to "abc",
            "HM_OEBS_API_PROXY_DB_PW" to "abc",
            "HM_OEBS_API_PROXY_DB_NAME" to "abc",

            "TOKEN_X_WELL_KNOWN_URL" to "abc",
            "TOKEN_X_CLIENT_ID" to "abc",

            "AZURE_APP_WELL_KNOWN_URL" to "abc",
            "AZURE_APP_CLIENT_ID" to "abc",

            "HJELPEMIDDELDATABASEN_API" to "abc",
        )
    )

    val oracleDatabaseConfig: Map<String, String> = mapOf(
        "HM_OEBS_API_PROXY_DB_NAME" to config()[Key("HM_OEBS_API_PROXY_DB_NAME", stringType)],
        "HM_OEBS_API_PROXY_DB_URL" to config()[Key("HM_OEBS_API_PROXY_DB_URL", stringType)],
        "HM_OEBS_API_PROXY_DB_USR" to config()[Key("HM_OEBS_API_PROXY_DB_USR", stringType)],
        "HM_OEBS_API_PROXY_DB_PW" to config()[Key("HM_OEBS_API_PROXY_DB_PW", stringType)],
    )

    val dataSource by lazy {
        println("ORACLE URL " + config()[Key("HM_OEBS_API_PROXY_DB_URL", stringType)])
        HikariDataSource().apply {
            username = config()[Key("HM_OEBS_API_PROXY_DB_USR", stringType)]
            password = config()[Key("HM_OEBS_API_PROXY_DB_PW", stringType)]
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
            driverClassName = "oracle.jdbc.driver.OracleDriver";
            jdbcUrl = config()[Key("HM_OEBS_API_PROXY_DB_URL", stringType)]
        }
    }

    val tokenX: Map<String, String> = mapOf(
        "TOKEN_X_WELL_KNOWN_URL" to config()[Key("TOKEN_X_WELL_KNOWN_URL", stringType)],
        "TOKEN_X_CLIENT_ID" to config()[Key("TOKEN_X_CLIENT_ID", stringType)],
    )

    val azureAD: Map<String, String> = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
        "AZURE_APP_CLIENT_ID" to config()[Key("AZURE_APP_CLIENT_ID", stringType)],
    )

    val application: Map<String, String> = mapOf(
        "APP_PROFILE" to config()[Key("application.profile", stringType)],
        "HJELPEMIDDELDATABASEN_API" to config()[Key("HJELPEMIDDELDATABASEN_API", stringType)],
    )

}
