package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Personinformasjon

class PersoninformasjonDao(private val tx: JdbcOperations) {
    fun hentPersoninformasjon(fnr: String): List<Personinformasjon> {
        return tx.list(
            """
                SELECT BRUKER_NUMMER, LEVERINGS_ADDRESSE, LEVERINGS_KOMMUNE, LEVERINGS_POSTNUMMER, LEVERINGS_BY, PRIMAER_ADR, BYDEL
                FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
                WHERE FNR = :fnr
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
