package no.nav.hjelpemidler.service.oebsdatabase

import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.client.hmdb.HjelpemiddeldatabaseClient
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Produkt
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class HjelpemiddeloversiktDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        @Language("OracleSQL")
        val query =
            """
            SELECT ANTALL, ENHET, KATEGORI3_BESKRIVELSE, ARTIKKEL_BESKRIVELSE, ARTIKKELNUMMER, 
                   SERIE_NUMMER, UTLÅNS_DATO, ORDRE_NUMMER, KATEGORI3_NUMMER, ARTIKKELSTATUS  
            FROM XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE FNR = ?
            ORDER BY UTLÅNS_DATO DESC
            """.trimIndent()

        val items = sessionOf(dataSource).use {
            it.run(
                queryOf(query, fnr).map { row ->
                    HjelpemiddelBruker(
                        antall = row.string("ANTALL"),
                        antallEnhet = row.string("ENHET"),
                        kategoriNummer = row.string("KATEGORI3_NUMMER"),
                        kategori = row.string("KATEGORI3_BESKRIVELSE"),
                        artikkelBeskrivelse = row.string("ARTIKKEL_BESKRIVELSE"),
                        artikkelNr = row.string("ARTIKKELNUMMER"),
                        serieNr = row.stringOrNull("SERIE_NUMMER"),
                        datoUtsendelse = row.string("UTLÅNS_DATO"),
                        ordrenummer = row.stringOrNull("ORDRE_NUMMER"),
                        artikkelStatus = row.string("ARTIKKELSTATUS")
                    )
                }.asList
            )
        }
        return berikOrdrelinjer(items)
    }

    fun harUtlåntIsokode(fnr: String, isokode: String): Boolean {
        @Language("OracleSQL")
        val query =
            """
            SELECT KATEGORI3_NUMMER  
            FROM XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE FNR = ?
            AND KATEGORI3_NUMMER = ?
            ORDER BY UTLÅNS_DATO DESC
            """.trimIndent()

        val items = sessionOf(dataSource).use {
            it.run(
                queryOf(query, fnr, isokode).map{row ->  row}.asList
            )
        }
        return items.isNotEmpty()
    }

    private fun berikOrdrelinjer(items: List<HjelpemiddelBruker>): List<HjelpemiddelBruker> = runBlocking {
        // Unique set of hmsnr to fetch data for
        val hmsnr = items.filter { it.artikkelNr.isNotEmpty() }.map { it.artikkelNr }.toSet()
        logg.info { "[DEBUG]: hmsnr: $hmsnr" }

        // Fetch data for hmsnr from hm-grunndata-api
        val produkter: List<Produkt> = HjelpemiddeldatabaseClient.hentProdukter(hmsnr)
        logg.info { "[DEBUG]: produkter: $produkter" }

        // Apply data to items
        val produkterByHmsnr = produkter.groupBy { it.hmsnr }
        logg.info { "[DEBUG]: produkterByHmsnr: $produkterByHmsnr" }
        items.map { item ->
            val produkt = produkterByHmsnr[item.artikkelNr]?.firstOrNull()
            if (produkt == null) {
                item
            } else {
                berikOrdrelinje(item, produkt)
            }
        }
    }

    private fun berikOrdrelinje(item: HjelpemiddelBruker, produkt: Produkt): HjelpemiddelBruker {
        item.apply {
            item.hmdbBeriket = true
            item.hmdbProduktNavn = produkt.artikkelnavn
            item.hmdbBeskrivelse = produkt.produktbeskrivelse
            item.hmdbKategori = produkt.isotittel
            item.hmdbBilde = produkt.blobUrlLite
            item.hmdbURL = produkt.artikkelUrl
            item.hmdbKategoriKortnavn = produkt.isokortnavn
        }
        return item
    }

    companion object {
        private val log = LoggerFactory.getLogger("HjelpemiddeloversiktDao")
    }
}
