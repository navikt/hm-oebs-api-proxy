package no.nav.hjelpemidler

import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.configuration.External
import no.nav.hjelpemidler.configuration.vaultSecret

object Configuration {
    val OEBS_BRUKER_ID by EnvironmentVariable

    val OEBS_API_URL by EnvironmentVariable

    @External
    val OEBS_API_TOKEN by EnvironmentVariable

    val GRUNNDATA_API_URL by EnvironmentVariable

    /**
     * Mulige verdier: 'oebsq1' | 'oebsq2' | 'oebst1' | 'oebsp'
     */
    val OEBS_DB by EnvironmentVariable

    @External
    val OEBS_DB_JDBC_URL by vaultSecret("/secrets/$OEBS_DB/config/jdbc_url")

    // JDBC_URL for T2 ligger i k8s secret, ikke i Vault.
    // val OEBS_DB_JDBC_URL_T2 by EnvironmentVariable

    @External
    val OEBS_DB_USERNAME by vaultSecret("/secrets/$OEBS_DB/credentials/username")

    @External
    val OEBS_DB_PASSWORD by vaultSecret("/secrets/$OEBS_DB/credentials/password")

    @External
    val NORG_API_URL by EnvironmentVariable
}
