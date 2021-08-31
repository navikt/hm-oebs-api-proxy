package no.nav.hjelpemidler.service.hjelpemiddeldatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Personinformasjon
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class PersoninformasjonDao (private val dataSource: DataSource = Configuration.dataSource) {

    fun hentPersoninformasjon(fnr: String): List<Personinformasjon>{

        @Language("OracleSQL")
        val hentPersoninfoQuery = """
            SELECT BRUKER_NUMMER, LEVERINGS_ADDRESSE, LEVERINGS_KOMMUNE, LEVERINGS_POSTNUMMER, LEVERINGS_BY, PRIMAER_ADR
            FROM XXRTV_DIGIHOT_OEBS_ADR_FNR_V
            WHERE FNR = ?
        """.trimIndent()

        val personinformasjonListe = sessionOf(dataSource).use {
            it.run(queryOf(hentPersoninfoQuery, fnr).map { row ->
                Personinformasjon(
                    brukerNr = row.string("BRUKER_NR"),
                    leveringAddresse = row.string("LEVERINGS_ADDRESSE"),
                    leveringPostnr = row.string("LEVERINGS_POSTNUMMER"),
                    leveringKommune = row.string("LEVERINGS_KOMMUNE"),
                    leveringBy = row.string("LEVERINGS_BY"),
                    primaerAdr = row.string("PRIMAER_ADR")
                )
            }.asList)
        }
        return personinformasjonListe
    }

}
