package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import javax.sql.DataSource

class BrukerpassDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun brukerpassForFnr(fnr: String): Brukerpass {
        @Language("Oracle")
        var query =
            """
                SELECT KONTRAKT_NUMMER, SJEKK_NAVN, START_DATE, END_DATE
                FROM apps.XXRTV_DIGIHOT_OEBS_BRUKERP_V
                WHERE FNR = ?
            """.trimIndent()

        return sessionOf(dataSource).use {
            it.run(
                queryOf(query, fnr).map { row ->
                    Brukerpass(
                        brukerpass = true,
                        kontraktNummer = row.stringOrNull("KONTRAKT_NUMMER"),
                        row.localDateOrNull("START_DATE"),
                        row.localDateOrNull("END_DATE"),
                    )
                }.asSingle,
            )
        } ?: Brukerpass(brukerpass = false)
    }
}

data class Brukerpass(
    val brukerpass: Boolean,
    val kontraktNummer: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
