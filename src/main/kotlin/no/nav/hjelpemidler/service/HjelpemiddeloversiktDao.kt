package no.nav.hjelpemidler.service

import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.client.GrunndataClient
import no.nav.hjelpemidler.client.hmdb.enums.MediaType
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.Utlån
import org.intellij.lang.annotations.Language

class HjelpemiddeloversiktDao(private val tx: JdbcOperations) {
    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        @Language("Oracle")
        val query = """
            SELECT antall,
                   enhet,
                   kategori3_nummer,
                   kategori3_beskrivelse,
                   artikkel_beskrivelse,
                   artikkelnummer,
                   serie_nummer,
                   utlåns_dato,
                   ordre_nummer,
                   artikkelstatus,
                   utlåns_type,
                   innleveringsdato,
                   oppdatert_innleveringsdato
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE fnr = :fnr
            ORDER BY utlåns_dato DESC
        """.trimIndent()

        val items = tx.list(query, mapOf("fnr" to fnr)) { row ->
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
        return tx.list(
            """
                SELECT kategori3_nummer, utlåns_dato
                FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
                WHERE fnr = :fnr
                  AND kategori3_nummer = :isokode
                ORDER BY utlåns_dato DESC
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
        return tx.singleOrNull(
            """
                SELECT fnr, artikkelnummer, serie_nummer, utlåns_dato
                FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
                WHERE artikkelnummer = :artnr
                  AND serie_nummer = :serienr
                ORDER BY utlåns_dato DESC
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
        return tx.list(
            """
            SELECT fnr  
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE artikkelnummer = :artnr
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
        val produkter: List<Product> = GrunndataClient.hentProdukter(hmsnr)

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
}
