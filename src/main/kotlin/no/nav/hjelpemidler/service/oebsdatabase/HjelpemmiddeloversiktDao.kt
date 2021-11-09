package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.Personinformasjon
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class HjelpemmiddeloversiktDao(private val dataSource: DataSource = Configuration.dataSource) {

    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {

        @Language("OracleSQL")
        val hentPersoninfoQuery =
            """
                SELECT ANTALL, ENHET, KATEGORI3_BESKRIVELSE, ARTIKKEL_BESKRIVELSE, ARTIKKELNUMMER, SERIE_NUMMER, FØRSTE_UTSENDELSE
                FROM XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
                WHERE FNR = ?
                ORDER BY FØRSTE_UTSENDELSE DESC
            """.trimIndent()

        val hjelpemiddeloversiktListe = sessionOf(dataSource).use {
            it.run(
                queryOf(hentPersoninfoQuery, fnr).map { row ->
                    HjelpemiddelBruker(
                        antall = row.string("ANTALL"),
                        antallEnhet = row.string("ENHET"),
                        kategori = row.string("KATEGORI3_BESKRIVELSE"),
                        artikkelBeskrivelse = row.string("ARTIKKEL_BESKRIVELSE"),
                        artikkelNr = row.string("ARTIKKELNUMMER"),
                        serieNr = row.string("SERIE_NUMMER"),
                        datoUtsendelse = row.string("FØRSTE_UTSENDELSE"),
                    )
                }.asList
            )
        }
        return hjelpemiddeloversiktListe
    }
}
