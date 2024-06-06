package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Personinformasjon

class PersoninformasjonDao(private val tx: JdbcOperations) {
    fun hentPersoninformasjon(fnr: String): List<Personinformasjon> {
        return tx.list(
            """
                SELECT bruker_nummer, leverings_addresse, leverings_kommune, leverings_postnummer, leverings_by, primaer_adr, bydel
                FROM apps.xxrtv_digihot_oebs_adr_fnr_v
                WHERE fnr = :fnr
            """.trimIndent(),
            mapOf("fnr" to fnr),
        ) { row ->
            Personinformasjon(
                brukerNr = row.string("BRUKER_NUMMER"),
                leveringAddresse = row.string("LEVERINGS_ADDRESSE"),
                leveringKommune = row.stringOrNull("LEVERINGS_KOMMUNE") ?: "",
                leveringPostnr = row.stringOrNull("LEVERINGS_POSTNUMMER") ?: "",
                leveringBy = row.stringOrNull("LEVERINGS_BY") ?: "",
                primaerAdr = row.string("PRIMAER_ADR"),
                bydel = row.stringOrNull("BYDEL"),
            )
        }
    }
}
