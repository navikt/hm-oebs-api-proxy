package no.nav.hjelpemidler.configuration

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource

internal object Configuration {
    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to "prod",

            "HM_OEBS_OPPRETT_SF_BRUKEROD" to "81760",

            "HM_OEBS_API_PROXY_DB_NAME" to "oebsp",
            "HM_OEBS_API_PROXY_DB_URL" to System.getenv("HM_OEBS_API_PROXY_DB_URL_P"),
            "HM_OEBS_API_PROXY_DB_USR" to System.getenv("HM_OEBS_API_PROXY_DB_USR_P"),
            "HM_OEBS_API_PROXY_DB_PW" to System.getenv("HM_OEBS_API_PROXY_DB_PW_P"),

            "GRUNNDATA_API_URL" to "https://hm-grunndata-api.intern.nav.no",
            "OEBS_API_URL" to "http://oebs.adeo.no/webservices/rest/opprettordre/digihotordreontinfo/"
        )
    )

    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to "dev",

            "HM_OEBS_OPPRETT_SF_BRUKEROD" to "81400",

//            "HM_OEBS_API_PROXY_DB_NAME" to "oebst1",
//            "HM_OEBS_API_PROXY_DB_URL" to System.getenv("HM_OEBS_API_PROXY_DB_URL_T1"),
//            "HM_OEBS_API_PROXY_DB_USR" to System.getenv("HM_OEBS_API_PROXY_DB_USR_T1"),
//            "HM_OEBS_API_PROXY_DB_PW" to System.getenv("HM_OEBS_API_PROXY_DB_PW_T1"),

            "HM_OEBS_API_PROXY_DB_NAME" to "oebsq1",
            "HM_OEBS_API_PROXY_DB_URL" to System.getenv("HM_OEBS_API_PROXY_DB_URL_Q1"),
            "HM_OEBS_API_PROXY_DB_USR" to System.getenv("HM_OEBS_API_PROXY_DB_USR_Q1"),
            "HM_OEBS_API_PROXY_DB_PW" to System.getenv("HM_OEBS_API_PROXY_DB_PW_Q1"),

            "GRUNNDATA_API_URL" to "https://hm-grunndata-api.intern.dev.nav.no",
            // T1
            // "OEBS_API_URL" to "http://d26apbl007.test.local:8086/webservices/rest/opprettordre/digihotordreontinfo/",
            // Q1
            "OEBS_API_URL" to "http://oebsq.preprod.local/webservices/rest/opprettordre/digihotordreontinfo/"
        )
    )

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.profile" to "local",

            "HM_OEBS_OPPRETT_SF_BRUKEROD" to "",

            "HM_OEBS_API_PROXY_DB_URL" to "",
            "HM_OEBS_API_PROXY_DB_USR" to "",
            "HM_OEBS_API_PROXY_DB_PW" to "",
            "HM_OEBS_API_PROXY_DB_NAME" to "",

            "TOKEN_X_WELL_KNOWN_URL" to "",
            "TOKEN_X_CLIENT_ID" to "",

            "AZURE_APP_WELL_KNOWN_URL" to "",
            "AZURE_APP_CLIENT_ID" to "",

            "GRUNNDATA_API_URL" to "http://host.docker.internal:8880",

            "OEBS_API_URL" to "",
            "OEBS_API_TOKEN" to ""
        )
    )

    private val configuration = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding devProperties
        "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding prodProperties
        else -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding localProperties
    }

    operator fun get(key: String): String = configuration[Key(key, stringType)]

    val oracleDatabaseConfig: Map<String, String> = mapOf(
        "HM_OEBS_API_PROXY_DB_NAME" to get("HM_OEBS_API_PROXY_DB_NAME"),
        "HM_OEBS_API_PROXY_DB_URL" to get("HM_OEBS_API_PROXY_DB_URL"),
        "HM_OEBS_API_PROXY_DB_USR" to get("HM_OEBS_API_PROXY_DB_USR"),
        "HM_OEBS_API_PROXY_DB_PW" to get("HM_OEBS_API_PROXY_DB_PW")
    )

    val dataSource by lazy {
        println("ORACLE URL " + get("HM_OEBS_API_PROXY_DB_URL"))
        HikariDataSource().apply {
            username = get("HM_OEBS_API_PROXY_DB_USR")
            password = get("HM_OEBS_API_PROXY_DB_PW")
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
            driverClassName = "oracle.jdbc.driver.OracleDriver"
            jdbcUrl = get("HM_OEBS_API_PROXY_DB_URL")
        }
    }

    val tokenX: Map<String, String> = mapOf(
        "TOKEN_X_WELL_KNOWN_URL" to get("TOKEN_X_WELL_KNOWN_URL"),
        "TOKEN_X_CLIENT_ID" to get("TOKEN_X_CLIENT_ID")
    )

    val azureAD: Map<String, String> = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to get("AZURE_APP_WELL_KNOWN_URL"),
        "AZURE_APP_CLIENT_ID" to get("AZURE_APP_CLIENT_ID")
    )

    val application: Map<String, String> = mapOf(
        "APP_PROFILE" to get("application.profile"),
        "GRUNNDATA_API_URL" to get("GRUNNDATA_API_URL"),
        "OEBS_BRUKER_ID" to get("HM_OEBS_OPPRETT_SF_BRUKEROD")
    )

    val oebsApi: Map<String, String> = mapOf(
        "OEBS_API_URL" to get("OEBS_API_URL"),
        "OEBS_API_TOKEN" to get("OEBS_API_TOKEN")
    )
}
