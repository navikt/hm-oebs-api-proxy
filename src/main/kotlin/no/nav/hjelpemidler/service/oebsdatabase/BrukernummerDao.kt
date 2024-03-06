package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Fødselsnummer
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class BrukernummerDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentBrukernummer(fnr: Fødselsnummer): Brukernummer? {
        @Language("OracleSQL")
        val hentBrukernummerQuery =
            """
            SELECT BRUKER_NUMMER
            FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
            WHERE FNR = ?
            """.trimIndent()

        val brukernummer = sessionOf(dataSource).use {
            it.run(
                queryOf(hentBrukernummerQuery, fnr.value).map { row ->
                    Brukernummer(
                        brukernummer = row.string("BRUKER_NUMMER"),
                    )
                }.asSingle,
            )
        }
        return brukernummer
    }
}

data class Brukernummer(val brukernummer: String)
