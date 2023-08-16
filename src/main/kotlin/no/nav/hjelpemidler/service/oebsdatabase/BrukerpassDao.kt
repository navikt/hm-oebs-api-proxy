package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class BrukerpassDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun brukerpassForFnr(fnr: String): Brukerpass {
        @Language("OracleSQL")
        var query =
            """
                SELECT KONTRAKT_NUMMER, SJEKK_NAVN, START_DATE, END_DATE
                FROM XXRTV_DIGIHOT_OEBS_BRUKERP_V
                WHERE FNR = ?
            """.trimIndent()

        return sessionOf(dataSource).use { it ->
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

    fun hentAlleBrukerpass(): List<BrukerpassMedFnr> {
        @Language("OracleSQL")
        var query =
            """
                SELECT FNR, KONTRAKT_NUMMER, SJEKK_NAVN, START_DATE, END_DATE
                FROM XXRTV_DIGIHOT_OEBS_BRUKERP_V
            """.trimIndent()

        val items = sessionOf(dataSource).use {
            it.run(
                queryOf(query).map { row ->
                    BrukerpassMedFnr(
                        fnr = row.string("FNR"),
                        brukerpass = true,
                        kontraktNummer = row.stringOrNull("KONTRAKT_NUMMER"),
                        row.localDateOrNull("START_DATE"),
                        row.localDateOrNull("END_DATE"),
                    )
                }.asList,
            )
        }

        return items
    }
}

data class Brukerpass(
    val brukerpass: Boolean,
    val kontraktNummer: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

data class BrukerpassMedFnr(
    val fnr: String,
    val brukerpass: Boolean,
    val kontraktNummer: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

fun testHelper(row: Row) {
    logg.info("DEBUG: Row start:")
    val c = row.underlying.metaData.columnCount
    for (i in 1..c) {
        logg.info("DEBUG: Column #$i")
        val label = row.underlying.metaData.getColumnLabel(i)
        val name = row.underlying.metaData.getColumnName(i)
        val type = row.underlying.metaData.getColumnTypeName(i)
        logg.info("DEBUG: Column #$i - $label/$name - $type")
    }
    logg.info("DEBUG: Row end.")
}
