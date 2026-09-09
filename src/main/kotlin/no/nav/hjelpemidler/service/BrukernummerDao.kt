package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.Configuration
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.models.Brukernummer

class BrukernummerDao(private val tx: JdbcOperations) {
    fun hentBrukernummer(fnr: Fødselsnummer): Brukernummer? = tx.singleOrNull(
        """
            SELECT bruker_nummer
            FROM apps.xxrtv_digihot_oebs_adr_fnr_v
            WHERE fnr = :fnr
            FETCH NEXT 1 ROW ONLY
        """.trimIndent(),
        mapOf("fnr" to fnr),
    ) { row ->
        Brukernummer(row.string("bruker_nummer"))
    }

    fun hentBrukernumre(fnr: Set<Fødselsnummer>): Map<Fødselsnummer, String?> {
        if (fnr.isEmpty()) return emptyMap()

        val schema = Configuration.OEBS_DB_USERNAME
        val temporaryTableName = $$"$$schema.ORA$PTT_fnr"
        tx.execute(
            """
                CREATE PRIVATE TEMPORARY TABLE $temporaryTableName
                (
                    fnr CHAR(11)
                ) ON COMMIT DROP DEFINITION
            """.trimIndent(),
        )
        tx.batch(
            "INSERT INTO $temporaryTableName (fnr) VALUES (:fnr)",
            fnr,
        ) { mapOf("fnr" to it) }

        return tx
            .list(
                """
                    SELECT t1.fnr, t2.bruker_nummer
                    FROM $temporaryTableName t1
                        LEFT JOIN apps.xxrtv_digihot_oebs_adr_fnr_v t2
                            ON t1.fnr = t2.fnr
                """.trimIndent(),
                mapOf("fnr" to fnr),
            ) { row ->
                row.fødselsnummer("fnr") to row.stringOrNull("bruker_nummer")
            }
            .toMap()
    }
}
