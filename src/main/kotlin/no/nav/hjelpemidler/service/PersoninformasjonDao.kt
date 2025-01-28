package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.Personinformasjon
import no.nav.hjelpemidler.models.adresse

class PersoninformasjonDao(private val tx: JdbcOperations) {
    fun hentPersoninformasjon(fnr: String): List<Personinformasjon> = tx.list(
        """
                SELECT bruker_nummer,
                       TRIM(bosteds_addresse)   AS bosteds_adresse,
                       bosteds_postnummer,
                       TRIM(bosteds_by)         AS bosteds_poststed,
                       bosteds_kommune,
                       TRIM(leverings_addresse) AS leverings_adresse,
                       leverings_postnummer,
                       TRIM(leverings_by)       AS leverings_poststed,
                       leverings_kommune,
                       TRIM(bydel)              AS bydel,
                       primaer_adr,
                       status_brukernr,
                       status_fnr
                FROM apps.xxrtv_digihot_oebs_adr_fnr_v
                WHERE fnr = :fnr
        """.trimIndent(),
        mapOf("fnr" to fnr),
    ) { row ->
        Personinformasjon(
            brukerNr = row.string("bruker_nummer"),
            bostedsadresse = row.adresse("bosteds"),
            leveringsadresse = row.adresse("leverings"),
            bydel = row.stringOrNull("bydel"),
            primaerAdr = row.string("primaer_adr"), // Y | N
            aktiv = row.stringOrNull("status_brukernr") == "A" && row.stringOrNull("status_fnr") == "A", // A | I
        )
    }
}
