package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.database.Configuration
import no.nav.hjelpemidler.database.list
import no.nav.hjelpemidler.models.Personinformasjon
import javax.sql.DataSource

class PersoninformasjonDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentPersoninformasjon(fnr: String): List<Personinformasjon> {
        return dataSource.list(
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
