package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Fødselsnummer

class BrukernummerDao(private val tx: JdbcOperations) {
    fun hentBrukernummer(fnr: Fødselsnummer): Brukernummer? {
        return tx.singleOrNull(
            """
                SELECT BRUKER_NUMMER
                FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
                WHERE FNR = :fnr
                FETCH NEXT 1 ROW ONLY
            """.trimIndent(),
            mapOf("fnr" to fnr.value),
        ) { row ->
            Brukernummer(
                brukernummer = row.string("BRUKER_NUMMER"),
            )
        }
    }
}

data class Brukernummer(val brukernummer: String)
