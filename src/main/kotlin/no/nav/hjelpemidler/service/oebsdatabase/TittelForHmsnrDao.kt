package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.TittelForHmsNr
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class TittelForHmsnrDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentTittelForHmsnr(hmsnr: String): TittelForHmsNr? {
        return hentTittelForHmsnrs(listOf(hmsnr).toSet()).firstOrNull()
    }

    fun hentTittelForHmsnrs(hmsnrs: Set<String>): List<TittelForHmsNr> {
        @Language("OracleSQL")
        val query_dev =
            """
                SELECT ARTIKKEL, BRUKERARTIKKELTYPE, ARTIKKEL_BESKRIVELSE
                FROM XXRTV_DIGIHOT_OEBS_ART_BESKR_V
                WHERE ARTIKKEL IN ?
            """.trimIndent()

        @Language("OracleSQL")
        val query_prod =
            """
                SELECT ARTIKKEL, ARTIKKEL_BESKRIVELSE
                FROM XXRTV_DIGIHOT_OEBS_ART_BESKR_V
                WHERE ARTIKKEL IN ?
            """.trimIndent()

        var query = query_prod
        if (Configuration.application["APP_PROFILE"]!! != "prod") {
            query = query_dev
        }

        return sessionOf(dataSource).use {
            it.run(
                queryOf(query, hmsnrs.toList()).map { row ->
                    if (Configuration.application["APP_PROFILE"]!! != "prod") {
                        TittelForHmsNr(
                            hmsNr = row.string("ARTIKKEL"),
                            type = row.string("BRUKERARTIKKELTYPE"),
                            title = row.string("ARTIKKEL_BESKRIVELSE"),
                        )
                    } else {
                        TittelForHmsNr(
                            hmsNr = row.string("ARTIKKEL"),
                            type = "<none>",
                            title = row.string("ARTIKKEL_BESKRIVELSE"),
                        )
                    }
                }.asList
            )
        }
    }
}
