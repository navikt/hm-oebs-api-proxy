package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Personinformasjon
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class PersoninformasjonDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentPersoninformasjon(fnr: String): List<Personinformasjon> {
        @Language("Oracle")
        val hentPersoninfoQuery =
            """
            SELECT BRUKER_NUMMER, LEVERINGS_ADDRESSE, LEVERINGS_KOMMUNE, LEVERINGS_POSTNUMMER, LEVERINGS_BY, PRIMAER_ADR, BYDEL
            FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
            WHERE FNR = ?
            """.trimIndent()

        val personinformasjonListe = sessionOf(dataSource).use {
            it.run(
                queryOf(hentPersoninfoQuery, fnr).map { row ->
                    Personinformasjon(
                        brukerNr = row.string("BRUKER_NUMMER"),
                        leveringAddresse = row.string("LEVERINGS_ADDRESSE"),
                        leveringPostnr = row.stringOrNull("LEVERINGS_POSTNUMMER") ?: "",
                        leveringKommune = row.stringOrNull("LEVERINGS_KOMMUNE") ?: "",
                        leveringBy = row.stringOrNull("LEVERINGS_BY") ?: "",
                        primaerAdr = row.string("PRIMAER_ADR"),
                        bydel = row.stringOrNull("BYDEL"),
                    )
                }.asList,
            )
        }
        return personinformasjonListe
    }
}
