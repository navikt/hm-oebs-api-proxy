package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.sql.Sql
import no.nav.hjelpemidler.models.TittelForHmsNr

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
        var query = Sql(
            """
                SELECT artikkel, brukerartikkeltype, artikkel_beskrivelse
                FROM apps.xxrtv_digihot_oebs_art_beskr_v
                WHERE artikkel IN (?)
            """.trimIndent(),
        )

        val queryParameters = hmsnrs
            .mapIndexed { index, hmsnr -> "a$index" to hmsnr }
            .toMap()

        // Put hmsnrs.size number of comma separated named parameters in the query IN-clause
        query = query.replace(
            "(?)",
            queryParameters.keys.joinToString(separator = ", ", prefix = "(", postfix = ")") { ":$it" },
        )

        return tx.list(query, queryParameters) { row ->
            TittelForHmsNr(
                hmsNr = row.string("artikkel"),
                type = row.string("brukerartikkeltype"),
                title = row.string("artikkel_beskrivelse"),
            )
        }
    }
}
