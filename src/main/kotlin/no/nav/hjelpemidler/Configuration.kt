package no.nav.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.configuration.External
import java.io.File

object Configuration {
    val OEBS_BRUKER_ID by EnvironmentVariable

    val OEBS_API_URL by EnvironmentVariable

    @External
    val OEBS_API_TOKEN by EnvironmentVariable

    val GRUNNDATA_API_URL by EnvironmentVariable

    /**
     * Mulige verdier: 'oebsq1' | 'oebst1' | 'oebsp'
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

    private fun vaultSecret(filename: String): Lazy<String> = lazy {
        runCatching { File(filename).readText(Charsets.UTF_8) }.getOrElse {
            log.warn(it) { "Kunne ikke lese filename: $filename" }
            ""
        }
    }

    private val log = KotlinLogging.logger {}
}

fun isProd(): Boolean = Environment.current.tier.isProd
fun isNotProd(): Boolean = !isProd()
