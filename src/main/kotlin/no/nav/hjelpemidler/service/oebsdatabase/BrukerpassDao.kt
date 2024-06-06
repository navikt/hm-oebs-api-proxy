package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.database.JdbcOperations
import java.time.LocalDate

class BrukerpassDao(private val tx: JdbcOperations) {
    fun brukerpassForFnr(fnr: String): Brukerpass {
        return tx.singleOrNull(
            """
                SELECT KONTRAKT_NUMMER, SJEKK_NAVN, START_DATE, END_DATE
                FROM apps.XXRTV_DIGIHOT_OEBS_BRUKERP_V
                WHERE FNR = :fnr
                FETCH NEXT 1 ROW ONLY
            """.trimIndent(),
            mapOf("fnr" to fnr),
        ) { row ->
            Brukerpass(
                brukerpass = true,
                kontraktNummer = row.stringOrNull("KONTRAKT_NUMMER"),
                row.localDateOrNull("START_DATE"),
                row.localDateOrNull("END_DATE"),
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
