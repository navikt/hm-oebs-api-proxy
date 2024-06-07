package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Personinformasjon

class PersoninformasjonDao(private val tx: JdbcOperations) {
    fun hentPersoninformasjon(fnr: String): List<Personinformasjon> {
        return tx.list(
            """
                SELECT bruker_nummer,
                       leverings_addresse,
                       leverings_kommune,
                       leverings_postnummer,
                       leverings_by,
                       primaer_adr,
                       bydel,
                       status_brukernr,
                       status_fnr
                FROM apps.xxrtv_digihot_oebs_adr_fnr_v
                WHERE fnr = :fnr
            """.trimIndent(),
            mapOf("fnr" to fnr),
        ) { row ->
            Personinformasjon(
                brukerNr = row.string("bruker_nummer"),
                leveringAddresse = row.string("leverings_addresse"),
                leveringKommune = row.stringOrNull("leverings_kommune") ?: "",
                leveringPostnr = row.stringOrNull("leverings_postnummer") ?: "",
                leveringBy = row.stringOrNull("leverings_by") ?: "",
                primaerAdr = row.string("primaer_adr"), // Y | N
                bydel = row.stringOrNull("bydel"),
                aktiv = row.stringOrNull("status_brukernr") == "A" && row.stringOrNull("status_fnr") == "A", // A | I
            )
        }
    }
}
