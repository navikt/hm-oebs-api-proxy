package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class TestDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun testNamespacing() {

        logg.info { "ENTER testNamespacing" }

        testBrukernummer()
        testUtlån()
        testArtBeskrivelse()
        testBrukerpass()

//            "select * from apps.XXRTV_DIGIHOT_UTVID_ART_V",

    }

    private fun testBrukernummer() {
        @Language("OracleSQL")
        val query =
            """
            SELECT BRUKER_NUMMER, FNR
            FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
            WHERE BRUKER_NUMMER = '4249639'
            """.trimIndent()

        val result = sessionOf(dataSource).use {

            it.run(
                queryOf(query).map { row ->
                    logg.info { row.string("FNR") }
                    row.string("BRUKER_NUMMER")
                }.asSingle
            )
        }

        logg.info { "Result testBrukernummer $result" }
    }

    private fun testUtlån() {
        @Language("OracleSQL")
        val query =
            """
            SELECT ARTIKKELNUMMER
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE FNR = '27066427779'
            """.trimIndent()

        val result = sessionOf(dataSource).use {

            it.run(
                queryOf(query).map { row ->
                    row.string("ARTIKKELNUMMER")
                }.asSingle
            )
        }

        logg.info { "Result testUtlån $result" }
    }

    private fun testArtBeskrivelse() {
        @Language("OracleSQL")
        var query =
            """
                SELECT ARTIKKEL, BRUKERARTIKKELTYPE, ARTIKKEL_BESKRIVELSE
                FROM apps.XXRTV_DIGIHOT_OEBS_ART_BESKR_V
                WHERE ARTIKKEL = '236958'
            """.trimIndent()

        val result = sessionOf(dataSource).use {

            it.run(
                queryOf(query).map { row ->
                    row.string("ARTIKKEL_BESKRIVELSE")
                }.asSingle
            )
        }

        logg.info { "Result testArtBeskrivelse $result}" }
    }

    private fun testBrukerpass() {
        @Language("OracleSQL")
        var query =
            """
                SELECT KONTRAKT_NUMMER, SJEKK_NAVN, START_DATE, END_DATE, FNR
                FROM apps.XXRTV_DIGIHOT_OEBS_BRUKERP_V
                WHERE KONTRAKT_NUMMER IS NOT NULL
            """.trimIndent()

        val result = sessionOf(dataSource).use {

            it.run(
                queryOf(query).map { row ->
                    row.string("FNR")
                }.asSingle
            )
        }

        logg.info { "Result testBrukerpass $result" }
    }
}
