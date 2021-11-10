package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.TittelForHmsNr
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class TittelForHmsnrDao(private val dataSource: DataSource = Configuration.dataSource) {

    fun hentTittelForHmsnr(hmsnr: String): TittelForHmsNr? {
        @Language("OracleSQL")
        val query =
            """
                SELECT ARTIKKEL, ARTIKKEL_BESKRIVELSE
                FROM XXRTV_DIGIHOT_OEBS_ART_BESKR_V
                WHERE ARTIKKEL = ?
            """.trimIndent()

        return sessionOf(dataSource).use {
            it.run(
                queryOf(query, hmsnr).map { row ->
                    TittelForHmsNr(
                        hmsNr = row.string("ARTIKKEL"),
                        title = row.string("ARTIKKEL_BESKRIVELSE"),
                    )
                }.asSingle
            )
        }
    }
}
