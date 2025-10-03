package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Brukerpass
import java.time.LocalDate

class BrukerpassDao(private val tx: JdbcOperations) {
    fun brukerpassForFnr(fnr: String): Brukerpass = tx.singleOrNull(
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
    }.let {
        // Mocks i dev
        if (it?.brukerpass != true &&
            !Environment.current.isProd &&
            listOf("26848497710", "15084300133", "03847797958").contains(fnr)
        ) {
            Brukerpass(
                brukerpass = true,
                kontraktNummer = "1234",
                LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(1),
            )
        } else {
            it ?: Brukerpass(brukerpass = false)
        }
    }
}
