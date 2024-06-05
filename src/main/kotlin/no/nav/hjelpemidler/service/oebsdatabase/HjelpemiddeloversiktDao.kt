package no.nav.hjelpemidler.service.oebsdatabase

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.client.hmdb.HjelpemiddeldatabaseClient
import no.nav.hjelpemidler.client.hmdb.enums.MediaType
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import no.nav.hjelpemidler.database.Configuration
import no.nav.hjelpemidler.database.list
import no.nav.hjelpemidler.database.singleOrNull
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.Utlån
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class HjelpemiddeloversiktDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        @Language("Oracle")
        val query = """
            SELECT ANTALL,
                   ENHET,
                   KATEGORI3_NUMMER,
                   KATEGORI3_BESKRIVELSE,
                   ARTIKKEL_BESKRIVELSE,
                   ARTIKKELNUMMER,
                   SERIE_NUMMER,
                   UTLÅNS_DATO,
                   ORDRE_NUMMER,
                   ARTIKKELSTATUS,
                   UTLÅNS_TYPE,
                   INNLEVERINGSDATO,
                   OPPDATERT_INNLEVERINGSDATO
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE FNR = :fnr
            ORDER BY UTLÅNS_DATO DESC
        """.trimIndent()

        val items = dataSource.list(query, mapOf("fnr" to fnr)) { row ->
            HjelpemiddelBruker(
                antall = row.string("ANTALL"),
                antallEnhet = row.string("ENHET"),
                kategoriNummer = row.string("KATEGORI3_NUMMER"),
                kategori = row.string("KATEGORI3_BESKRIVELSE"),
                artikkelBeskrivelse = row.string("ARTIKKEL_BESKRIVELSE"),
                artikkelNr = row.string("ARTIKKELNUMMER"),
                serieNr = row.stringOrNull("SERIE_NUMMER"),
                datoUtsendelse = row.stringOrNull("UTLÅNS_DATO"),
                ordrenummer = row.stringOrNull("ORDRE_NUMMER"),
                artikkelStatus = row.string("ARTIKKELSTATUS"),
                utlånsType = row.stringOrNull("UTLÅNS_TYPE"),
                innleveringsdato = row.stringOrNull("INNLEVERINGSDATO"),
                oppdatertInnleveringsdato = row.stringOrNull("OPPDATERT_INNLEVERINGSDATO"),
            )
        }

        return berikOrdrelinjer(items)
    }

    fun utlånPåIsokode(fnr: String, isokode: String): List<UtlånPåIsokode> {
        return dataSource.list(
            """
                SELECT KATEGORI3_NUMMER, UTLÅNS_DATO
                FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
                WHERE FNR = :fnr
                  AND KATEGORI3_NUMMER = :isokode
                ORDER BY UTLÅNS_DATO DESC
            """.trimIndent(),
            mapOf("fnr" to fnr, "isokode" to isokode),
        ) { row ->
            UtlånPåIsokode(
                kategoriNummer = row.string("KATEGORI3_NUMMER"),
                datoUtsendelse = row.string("UTLÅNS_DATO"),
            )
        }
    }

    fun utlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        return dataSource.singleOrNull(
            """
                SELECT FNR, ARTIKKELNUMMER, SERIE_NUMMER, UTLÅNS_DATO
                FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
                WHERE ARTIKKELNUMMER = :artnr
                  AND SERIE_NUMMER = :serienr
                ORDER BY UTLÅNS_DATO DESC
                FETCH NEXT 1 ROW ONLY
            """.trimIndent(),
            mapOf("artnr" to artnr, "serienr" to serienr),
        ) { row ->
            Utlån(
                fnr = row.string("FNR"),
                artnr = row.string("ARTIKKELNUMMER"),
                serienr = row.string("SERIE_NUMMER"),
                utlånsDato = row.string("UTLÅNS_DATO"),
            )
        }
    }

    fun utlånPåArtnr(artnr: String): List<String> {
        return dataSource.list(
            """
            SELECT FNR  
            FROM apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
            WHERE ARTIKKELNUMMER = :artnr
            """.trimIndent(),
            mapOf("artnr" to artnr),
        ) { row ->
            row.string("FNR")
        }
    }

    data class UtlånPåIsokode(
        val kategoriNummer: String,
        val datoUtsendelse: String,
    )

    private fun berikOrdrelinjer(items: List<HjelpemiddelBruker>): List<HjelpemiddelBruker> = runBlocking {
        // Unique set of hmsnr to fetch data for
        val hmsnr = items.filter { it.artikkelNr.isNotEmpty() }.map { it.artikkelNr }.toSet()

        // Fetch data for hmsnr from hm-grunndata-api
        val produkter: List<Product> = HjelpemiddeldatabaseClient.hentProdukter(hmsnr)

        // Apply data to items
        val produkterByHmsnr = produkter.groupBy { it.hmsArtNr }
        items.map { item ->
            berikBytteinfo(item)

            // Sorted by identifier (artid, like old grunndata-api), but still get the ACTIVE one if there are higher sorted INACTIVE ones.
            val produkt = produkterByHmsnr[item.artikkelNr]?.sortedBy { it.identifier }?.minByOrNull { it.status }
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
