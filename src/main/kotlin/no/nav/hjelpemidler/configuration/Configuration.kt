package no.nav.hjelpemidler.configuration

import com.natpryce.konfig.*

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
            "HM_OEBS_API_PROXY_DB_NAME" to "abc",

            "HM_OEBS_API_PROXY_DB_URL" to "abc",
            "HM_OEBS_API_PROXY_DB_USR" to "abc",
            "HM_OEBS_API_PROXY_DB_PW" to "abc",
            "HM_OEBS_API_PROXY_DB_NAME" to "abc",
        )
    )

    private val devProperties = ConfigurationMap(
        mapOf(
            "HM_OEBS_API_PROXY_DB_NAME" to "abc",

            "HM_OEBS_API_PROXY_DB_URL" to "abc",
            "HM_OEBS_API_PROXY_DB_USR" to "abc",
            "HM_OEBS_API_PROXY_DB_PW" to "abc",
            "HM_OEBS_API_PROXY_DB_NAME" to "abc",
        )
    )

    private val localProperties = ConfigurationMap(
        mapOf(
            "HM_OEBS_API_PROXY_DB_URL" to "abc",
            "HM_OEBS_API_PROXY_DB_USR" to "abc",
            "HM_OEBS_API_PROXY_DB_PW" to "abc",
            "HM_OEBS_API_PROXY_DB_NAME" to "abc",

            "AZURE_APP_WELL_KNOWN_URL" to "abc",
            "AZURE_APP_CLIENT_ID" to "abc",
        )
    )

    val oracleDatabaseConfig: Map<String, String> = mapOf(
        "HM_OEBS_API_PROXY_DB_URL" to config()[Key("HM_OEBS_API_PROXY_DB_URL", stringType)],
        "HM_OEBS_API_PROXY_DB_USR" to config()[Key("HM_OEBS_API_PROXY_DB_USR", stringType)],
        "HM_OEBS_API_PROXY_DB_PW" to config()[Key("HM_OEBS_API_PROXY_DB_PW", stringType)],
        "HM_OEBS_API_PROXY_DB_NAME" to config()[Key("HM_OEBS_API_PROXY_DB_NAME", stringType)],
    )

    val azureAD: Map<String, String> = mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
        "AZURE_APP_CLIENT_ID" to config()[Key("AZURE_APP_CLIENT_ID", stringType)],
    )

}