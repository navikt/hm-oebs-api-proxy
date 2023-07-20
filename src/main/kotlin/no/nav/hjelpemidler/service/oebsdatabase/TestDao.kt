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

        val testQueries = listOf<String>(
//            "select * from apps.XXRTV_DIGIHOT_OEBS_ART_BESKR_V",
//            "select * from apps.XXRTV_DIGIHOT_OEBS_BRUKERP_V",
//            "select * from apps.XXRTV_DIGIHOT_UTVID_ART_V",
        )

    }

    private fun testBrukernummer() {
        @Language("OracleSQL")
        val hentBrukernummerQuery =
            """
            SELECT BRUKER_NUMMER, FNR
            FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
            WHERE BRUKER_NUMMER = '4249639'
            """.trimIndent()

        val result = sessionOf(dataSource).use {

            it.run(
                queryOf(hentBrukernummerQuery).map { row ->
                    row.string("BRUKER_NUMMER")
                }.asSingle
            )
        }

        logg.info { "Result testBrukernummer $result" }
    }

    private fun testUtlån() {
        @Language("OracleSQL")
        val hentBrukernummerQuery =
            """
            SELECT FNR  
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            """.trimIndent()

        val result = sessionOf(dataSource).use {

            it.run(
                queryOf(hentBrukernummerQuery).map { row ->
                    row.string("FNR")
                }.asList
            )
        }

        logg.info { "Result testUtlån ${result.first()}" }
    }
}
