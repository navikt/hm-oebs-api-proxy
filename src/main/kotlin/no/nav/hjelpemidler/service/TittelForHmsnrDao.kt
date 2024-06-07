package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.TittelForHmsNr
import org.intellij.lang.annotations.Language

class TittelForHmsnrDao(private val tx: JdbcOperations) {
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
        @Language("Oracle")
        var query =
            """
                SELECT artikkel, brukerartikkeltype, artikkel_beskrivelse
                FROM apps.xxrtv_digihot_oebs_art_beskr_v
                WHERE artikkel IN (?)
            """.trimIndent()

        // Put hmsnrs.count() number of comma separated question marks in the query IN-clause
        query = query.replace("(?)", "(" + (0 until hmsnrs.count()).joinToString { "?" } + ")")

        return tx.list(query, *hmsnrs.toTypedArray()) { row ->
            TittelForHmsNr(
                hmsNr = row.string("artikkel"),
                type = row.string("brukerartikkeltype"),
                title = row.string("artikkel_beskrivelse"),
            )
        }
    }
}
