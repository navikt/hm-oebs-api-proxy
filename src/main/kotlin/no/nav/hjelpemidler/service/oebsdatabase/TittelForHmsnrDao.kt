package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.configuration.Configuration.dataSource
import no.nav.hjelpemidler.models.TittelForHmsNr
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class TittelForHmsnrDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentTittelForHmsnr(hmsnr: String): TittelForHmsNr? {
        return hentTittelForHmsnrs(listOf(hmsnr).toSet()).firstOrNull()
    }

    fun hentTittelForHmsnrs(hmsnrs: Set<String>): List<TittelForHmsNr> {
        // Chunking solves: java.sql.SQLSyntaxErrorException: ORA-01795: maks. antall uttrykk i en liste er 1000
        val chunks = hmsnrs.chunked(500)
        val results = mutableListOf<TittelForHmsNr>()
        for (chunk in chunks) {
            results.addAll(helper(chunk.toSet()))
        }
        return results
    }

    private fun helper(hmsnrs: Set<String>): List<TittelForHmsNr> {
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
                    TittelForHmsNr(
                        hmsNr = row.string("ARTIKKEL"),
                        type = row.string("BRUKERARTIKKELTYPE"),
                        title = row.string("ARTIKKEL_BESKRIVELSE"),
                    )
                }.asList
            )
        }
    }
}
