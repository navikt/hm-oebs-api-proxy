package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.models.Brukernummer

class BrukernummerDao(private val tx: JdbcOperations) {
    fun hentBrukernummer(fnr: Fødselsnummer): Brukernummer? {
        return tx.singleOrNull(
            """
                SELECT bruker_nummer
                FROM apps.xxrtv_digihot_oebs_adr_fnr_v
                WHERE fnr = :fnr
                FETCH NEXT 1 ROW ONLY
            """.trimIndent(),
            mapOf("fnr" to fnr),
        ) { row ->
            Brukernummer(row.string("bruker_nummer"))
        }
    }
}
