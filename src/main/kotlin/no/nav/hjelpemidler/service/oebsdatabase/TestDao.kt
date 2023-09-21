package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Resultat
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.Stønadsklasse
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class TestDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun testNamespacing() {
        logg.info { "ENTER testNamespacing" }

        // testSelectAll()

//         testBrukernummer()
//         testUtlån()
//         testArtBeskrivelse()
//         testBrukerpass()
//         testLagerstatus()
//        testPersoninformasjon()
        try {
            opprettServiceforespørsel(
                Serviceforespørsel(
                    fødselsnummer = "26848497710",
                    navn = "Berømt Aktivitet",
                    stønadsklasse = Stønadsklasse.HJDABH,
                    resultat = Resultat.I,
                    referansenummer = "9000",
                    )
            )
            logg.info { "Opprettelse av SF vellykket" }
        } catch (e: Exception) {
            logg.error(e) { "Opprettelse av SF feilet" }
        }

    }

    fun opprettServiceforespørsel(sf: Serviceforespørsel) {
        @Language("OracleSQL")
        val opprettSFQuery =
            """
            
            insert into xxrtv_cs_digihot_sf_opprett 
            (ID, FNR, NAVN, STONADSKLASS,SAKSTYPE,RESULTAT,SFDATO ,REFERANSENUMMER,KILDE,PROCESSED,LAST_UPDATE_DATE,LAST_UPDATED_BY,CREATION_DATE,CREATED_BY, JOB_ID, SAKSBLOKK)
            values 
            (XXRTV_CS_DIGIHOT_SF_OPPRETT_S.nextval, :fnr, :navn, :stonadsklasse, :sakstype, :resultat, sysdate, :referansenummer, :kilde, :processed, sysdate, :oppdatertAv, sysdate, :oppdatertAv, :jobId, 'X')
            """.trimIndent()

        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    opprettSFQuery,
                    mapOf(
                        "fnr" to sf.fødselsnummer,
                        "navn" to sf.navn,
                        "stonadsklasse" to sf.stønadsklasse.name,
                        "sakstype" to "S",
                        "resultat" to sf.resultat.name,
                        "referansenummer" to sf.referansenummer,
                        "kilde" to sf.kilde,
                        "processed" to "N",
                        "oppdatertAv" to Configuration.application["OEBS_BRUKER_ID"],
                        "jobId" to -1
                    )
                ).asUpdate
            )
        }
    }

    private fun testSelectAll() {
        logg.info { "test select *" }
        @Language("OracleSQL")
        val query =
            """
            SELECT *
            FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
            """.trimIndent()

        val result = sessionOf(dataSource).use {
            it.run(
                queryOf(query).map { row ->
                    row.string("BRUKER_NUMMER")
                }.asSingle,
            )
        }

        logg.info { "Result testSelectAll $result" }
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
                    row.string("BRUKER_NUMMER")
                }.asSingle,
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
                }.asSingle,
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
                }.asSingle,
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
                }.asSingle,
            )
        }

        logg.info { "Result testBrukerpass $result" }
    }

    private fun testLagerstatus() {
        @Language("OracleSQL")
        var query =
            """
                SELECT
                    organisasjons_id,
                    organisasjons_navn,
                    artikkelnummer,
                    artikkelid,
                    fysisk,
                    tilgjengeligatt,
                    tilgjengeligroo,
                    tilgjengelig,
                    behovsmeldt,
                    reservert,
                    restordre,
                    bestillinger,
                    anmodning,
                    intanmodning,
                    forsyning,
                    sortiment,
                    lagervare,
                    minmax
                FROM apps.XXRTV_DIGIHOT_UTVID_ART_V
                WHERE artikkelnummer = '236958'
            """.trimIndent()

        val result = sessionOf(dataSource).use {
            it.run(
                queryOf(query).map { row ->
                    row.string("organisasjons_id")
                }.asSingle,
            )
        }

        logg.info { "Result testLagerstatus $result" }
    }

    private fun testPersoninformasjon() {
        @Language("OracleSQL")
        var query =
            """
                SELECT *
                FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
                WHERE FNR = '27066427779'
            """.trimIndent()

        val result = sessionOf(dataSource).use {
            it.run(
                queryOf(query).map { row ->
                    val metadata = row.underlying.metaData
                    val columnsNumber: Int = metadata.getColumnCount()
                    while (row.underlying.next()) {
                        for (i in 1..columnsNumber) {
                            if (i > 1) print(",  ")
                            val columnValue: String = row.underlying.getString(i)
                            print(columnValue + " " + metadata.getColumnName(i))
                        }
                        println("")
                    }

                    row.string("FNR")
                }.asSingle,
            )
        }

        logg.info { "Result testBrukerpass $result" }
    }
}
