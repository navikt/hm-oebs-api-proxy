package no.nav.hjelpemidler.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.client.GrunndataClient
import no.nav.hjelpemidler.client.hmdb.enums.MediaType
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.sql.Sql
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.Utlån

private val log = KotlinLogging.logger {}

class HjelpemiddeloversiktDao(private val tx: JdbcOperations) {

    init {
        val artnr = "021788"
        val serienr = "070040"
        log.info { "DEBUG: Henter utlånsoversikt for låntager av artnr=$artnr serienr=$serienr" }
        val utlån = utlånPåArtnrOgSerienr(artnr = artnr, serienr = serienr)
        if (utlån == null) {
            log.info { "Fant ikke utlånet for artnr=$artnr serienr=$serienr" }
        } else {
            val hjelpemiddeloversikt = hentHjelpemiddeloversikt(utlån.fnr)
            log.info { "Fant utlånsoversikt=$hjelpemiddeloversikt" }
        }
    }

    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        val query = Sql(
            """
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
            """.trimIndent(),
        )

        val items = tx.list(query, mapOf("fnr" to fnr)) { row ->
            HjelpemiddelBruker(
                antall = row.string("antall"),
                antallEnhet = row.string("enhet"),
                kategoriNummer = row.string("kategori3_nummer"),
                kategori = row.string("kategori3_beskrivelse"),
                artikkelBeskrivelse = row.string("artikkel_beskrivelse"),
                artikkelNr = row.string("artikkelnummer"),
                serieNr = row.stringOrNull("serie_nummer"),
                datoUtsendelse = row.stringOrNull("utlåns_dato"),
                ordrenummer = row.stringOrNull("ordre_nummer"),
                artikkelStatus = row.string("artikkelstatus"),
                utlånsType = row.stringOrNull("utlåns_type"),
                innleveringsdato = row.stringOrNull("innleveringsdato"),
                oppdatertInnleveringsdato = row.stringOrNull("oppdatert_innleveringsdato"),
            )
        }

        log.info { "Innbyggers hjelpemiddeloversikt før beriking: $items" }

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
                kategoriNummer = row.string("kategori3_nummer"),
                datoUtsendelse = row.string("utlåns_dato"),
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
                fnr = row.string("fnr"),
                artnr = row.string("artikkelnummer"),
                serienr = row.string("serie_nummer"),
                utlånsDato = row.string("utlåns_dato"),
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
            row.string("fnr")
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
