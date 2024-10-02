package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Brukerpass
import java.time.LocalDate

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
        } ?: run {
            if (Environment.current.tier.isDev && fnr == "13820599335") {
                // Mock brukerpass for testbruker i dev
                Brukerpass(
                    brukerpass = true,
                    kontraktNummer = "1234",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1),
                )
            } else {
                Brukerpass(brukerpass = false)
            }
        }
    }
}
