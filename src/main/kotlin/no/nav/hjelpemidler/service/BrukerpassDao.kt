package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Brukerpass

class BrukerpassDao(private val tx: JdbcOperations) {
    fun brukerpassForFnr(fnr: String): Brukerpass {
        return tx.singleOrNull(
            """
                SELECT kontrakt_nummer, start_date, end_date
                FROM apps.xxrtv_digihot_oebs_brukerp_v
                WHERE fnr = :fnr
                FETCH NEXT 1 ROW ONLY
            """.trimIndent(),
            mapOf("fnr" to fnr),
        ) { row ->
            Brukerpass(
                brukerpass = true,
                kontraktNummer = row.stringOrNull("kontrakt_nummer"),
                row.localDateOrNull("start_date"),
                row.localDateOrNull("end_date"),
            )
        } ?: Brukerpass(brukerpass = false)
    }
}
