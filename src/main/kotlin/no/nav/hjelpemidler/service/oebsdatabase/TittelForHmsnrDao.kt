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
        var query =
            """
                SELECT ARTIKKEL, BRUKERARTIKKELTYPE, ARTIKKEL_BESKRIVELSE
                FROM XXRTV_DIGIHOT_OEBS_ART_BESKR_V
                WHERE ARTIKKEL IN (?)
            """.trimIndent()

        // Put hmsnrs.count() number of comma separated question marks in the query IN-clause
        query = query.replace("(?)", "(" + (0 until hmsnrs.count()).joinToString { "?" } + ")")

        return sessionOf(dataSource).use { it ->
            it.run(
                queryOf(query, params = hmsnrs.toTypedArray()).map { row ->
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
