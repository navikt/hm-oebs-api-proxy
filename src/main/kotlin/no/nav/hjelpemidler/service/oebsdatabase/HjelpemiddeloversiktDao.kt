package no.nav.hjelpemidler.service.oebsdatabase

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.hjelpemidler.client.hmdb.HjelpemiddeldatabaseClient
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Produkt
import no.nav.hjelpemidler.client.hmdbng.HjelpemiddeldatabaseNgClient
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

    data class UtlånPåIsokode(
        val kategoriNummer: String,
        val datoUtsendelse: String,
    )

    private fun berikOrdrelinjer(items: List<HjelpemiddelBruker>): List<HjelpemiddelBruker> = runBlocking {
        // Unique set of hmsnr to fetch data for
        val hmsnr = items.filter { it.artikkelNr.isNotEmpty() }.map { it.artikkelNr }.toSet()

        // Fetch data for hmsnr from hm-grunndata-api
        val produkter: List<Produkt> = HjelpemiddeldatabaseClient.hentProdukter(hmsnr)

        // TODO: Remove when old grunndata-api is replaced in prod., and old hmdb is hmdb-ng
        runCatching {
            val produkterMap = produkter.groupBy { it.hmsnr!! }.mapValues { it.value.first() }
            val produkterNgMap = runCatching { HjelpemiddeldatabaseNgClient.hentProdukter(produkterMap.map { it.key }.toSet()) }.getOrElse { e ->
                log2.error(e) { "DEBUG GRUNNDATA: Exception while fetching hmdb-ng: $e" }
                listOf()
            }.groupBy { it.hmsArtNr!! }.mapValues { it.value.sortedBy { it.identifier }.minByOrNull { it.status } }

            val missingHmsnrs: MutableList<String> = mutableListOf()
            val unexpectedDataHmsnrs: MutableMap<String, Pair<String, String>> = mutableMapOf()
            val matchesHmsnrs: MutableMap<String, String> = mutableMapOf()
            produkterMap.forEach { (hmsnr, old) ->
                val new = produkterNgMap[hmsnr]
                if (new == null) {
                    missingHmsnrs.add(hmsnr)
                } else if (
                    old.artikkelnavn != new.articleName ||
                    old.isotittel != new.isoCategoryTitle ||
                    old.isokortnavn != new.isoCategoryTitleShort ||
                    old.produktbeskrivelse != new.attributes.text
                ) {
                    unexpectedDataHmsnrs[hmsnr] = Pair(old.toString(), new.toString())
                } else {
                    matchesHmsnrs[hmsnr] = old.isokortnavn.toString()
                }
            }
            if (missingHmsnrs.isNotEmpty()) {
                log.info("DEBUG GRUNNDATA: new dataset missing results for hmsnrs=$missingHmsnrs")
            }
            if (unexpectedDataHmsnrs.isNotEmpty()) {
                log.info("DEBUG GRUNNDATA: new dataset has mismatching data: $unexpectedDataHmsnrs")
            }
            if (matchesHmsnrs.isNotEmpty()) {
                log.info("DEBUG GRUNNDATA: new dataset matches old for hmsnrs/data: $matchesHmsnrs")
            }
        }.getOrNull()

        // Apply data to items
        val produkterByHmsnr = produkter.groupBy { it.hmsnr }
        items.map { item ->
            berikBytteinfo(item)

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
        private val log2 = KotlinLogging.logger {}
    }
}
