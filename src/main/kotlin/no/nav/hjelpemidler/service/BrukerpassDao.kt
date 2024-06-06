package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import java.time.LocalDate

class BrukerpassDao(private val tx: JdbcOperations) {
    fun brukerpassForFnr(fnr: String): Brukerpass {
        return tx.singleOrNull(
            """
                SELECT kontrakt_nummer, sjekk_navn, start_date, end_date
                FROM apps.xxrtv_digihot_oebs_brukerp_v
                WHERE fnr = :fnr
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
