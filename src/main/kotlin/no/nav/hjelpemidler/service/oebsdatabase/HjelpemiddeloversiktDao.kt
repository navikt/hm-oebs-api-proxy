package no.nav.hjelpemidler.service.oebsdatabase

import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.client.hmdb.HjelpemiddeldatabaseClient
import no.nav.hjelpemidler.client.hmdb.enums.MediaType
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.Utlån
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class HjelpemiddeloversiktDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        @Language("OracleSQL")
        val query =
            """
            SELECT ANTALL, ENHET, KATEGORI3_BESKRIVELSE, ARTIKKEL_BESKRIVELSE, ARTIKKELNUMMER, 
                   SERIE_NUMMER, UTLÅNS_DATO, ORDRE_NUMMER, KATEGORI3_NUMMER, ARTIKKELSTATUS, 
                   UTLÅNS_TYPE, INNLEVERINGSDATO, OPPDATERT_INNLEVERINGSDATO
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
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
                        artikkelStatus = row.string("ARTIKKELSTATUS"),
                        utlånsType = row.stringOrNull("UTLÅNS_TYPE"),
                        innleveringsdato = row.stringOrNull("INNLEVERINGSDATO"),
                        oppdatertInnleveringsdato = row.stringOrNull("OPPDATERT_INNLEVERINGSDATO")
                    )
                }.asList
            )
        }
        return berikOrdrelinjer(items)
    }

    fun utlånPåIsokode(fnr: String, isokode: String): List<UtlånPåIsokode> {
        @Language("OracleSQL")
        val query =
            """
            SELECT KATEGORI3_NUMMER, UTLÅNS_DATO
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE FNR = ?
            AND KATEGORI3_NUMMER = ?
            ORDER BY UTLÅNS_DATO DESC
            """.trimIndent()

        val items = sessionOf(dataSource).use {
            it.run(
                queryOf(query, fnr, isokode).map { row ->
                    UtlånPåIsokode(
                        kategoriNummer = row.string("KATEGORI3_NUMMER"),
                        datoUtsendelse = row.string("UTLÅNS_DATO")
                    )
                }.asList
            )
        }

        return items
    }

    fun utlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        @Language("OracleSQL")
        val query =
            """
            SELECT FNR, ARTIKKELNUMMER, SERIE_NUMMER, UTLÅNS_DATO  
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE ARTIKKELNUMMER = ?
            AND SERIE_NUMMER = ?
            ORDER BY UTLÅNS_DATO DESC
            """.trimIndent()

        val item = sessionOf(dataSource).use {
            it.run(
                queryOf(query, artnr, serienr).map { row ->
                    Utlån(
                        fnr = row.string("FNR"),
                        artnr = row.string("ARTIKKELNUMMER"),
                        serienr = row.string("SERIE_NUMMER"),
                        utlånsDato = row.string("UTLÅNS_DATO")
                    )
                }.asSingle
            )
        }

        return item
    }

    data class UtlånPåIsokode(
        val kategoriNummer: String,
        val datoUtsendelse: String
    )

    private fun berikOrdrelinjer(items: List<HjelpemiddelBruker>): List<HjelpemiddelBruker> = runBlocking {
        // Unique set of hmsnr to fetch data for
        val hmsnr = items.filter { it.artikkelNr.isNotEmpty() }.map { it.artikkelNr }.toSet()

        // Fetch data for hmsnr from hm-grunndata-api
        val produkter: List<Product> = HjelpemiddeldatabaseClient.hentProdukter(hmsnr)

        // Apply data to items
        val produkterByHmsnr = produkter.groupBy { it.hmsArtNr }
        items.map { item ->
            val produkt = produkterByHmsnr[item.artikkelNr]?.firstOrNull()
            if (produkt == null) {
                item
            } else {
                berikOrdrelinje(item, produkt)
            }
        }
    }

    private fun berikOrdrelinje(item: HjelpemiddelBruker, produkt: Product): HjelpemiddelBruker {
        item.apply {
            item.hmdbBeriket = true
            item.hmdbProduktNavn = produkt.articleName
            item.hmdbBeskrivelse = produkt.attributes.text
            item.hmdbKategori = produkt.isoCategoryTitle
            item.hmdbBilde = produkt.media
                .filter { it.type == MediaType.IMAGE }
                .minByOrNull { it.priority }
                ?.uri?.let {
                    "https://finnhjelpemiddel.nav.no/imageproxy/400d/$it"
                }
            item.hmdbURL = produkt.productVariantURL
            item.hmdbKategoriKortnavn = produkt.isoCategoryTitleShort
        }
        return item
    }

    companion object {
        private val log = LoggerFactory.getLogger("HjelpemiddeloversiktDao")
        private val log2 = KotlinLogging.logger {}
    }
}
