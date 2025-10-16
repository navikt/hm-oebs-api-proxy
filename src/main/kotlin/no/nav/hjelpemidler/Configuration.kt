package no.nav.hjelpemidler

import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.configuration.External
import no.nav.hjelpemidler.configuration.vaultSecret

object Configuration {
    val OEBS_BRUKER_ID by EnvironmentVariable

    val GRUNNDATA_API_URL by EnvironmentVariable

    /**
     * Mulige verdier: 'q1' | 'q2' | 't1' | 'u1' | 'p'
     */
    val OEBS_MILJO by EnvironmentVariable

    @External
    val OEBS_API_TOKEN by vaultSecret("/secrets/$OEBS_MILJO/oebs/apps-user/token")

    @External
    val OEBS_API_URL by vaultSecret("/secrets/$OEBS_MILJO/oebs/apps-user/url")

    @External
    val OEBS_DB_JDBC_URL by vaultSecret("/secrets/$OEBS_MILJO/oebs/db-user/url")

    @External
    val OEBS_DB_USERNAME by vaultSecret("/secrets/$OEBS_MILJO/oebs/db-user/username")

    @External
    val OEBS_DB_PASSWORD by vaultSecret("/secrets/$OEBS_MILJO/oebs/db-user/password")

    @External
    val NORG_API_URL by EnvironmentVariable
}
