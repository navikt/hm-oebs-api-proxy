package no.nav.hjelpemidler.service.oebsdatabase

import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.client.`hmdb-ng`.HjelpemiddeldatabaseNgClient
import no.nav.hjelpemidler.client.hmdb.HjelpemiddeldatabaseClient
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Produkt
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import no.nav.hjelpemidler.models.Utlån
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

class HjelpemiddeloversiktDao() {
    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        return listOf(
            HjelpemiddelBruker(
                antall = "1",
                antallEnhet = "STK",
                kategoriNummer = "123903",
                kategori = "Stokker for mobility og markering",
                artikkelBeskrivelse = "",
                artikkelNr = "174378",
                serieNr = null,
                datoUtsendelse = "2021-04-14 11:02:34",
                hmdbBeriket = true,
                hmdbProduktNavn = "Svarovsky AB Mobilitystokk 2-delt teleskop",
                hmdbBeskrivelse = "2-delt teleskop orienteringsstokk, som kan slåes sammen til markeringsstokk. Håndtaket er mahogni imitert treverk, men kan også leveres med gummi-håndtak eller skinn-håndtak. Stokken er fast og har fin balanse. Stokken kan leveres med alle typer tupper.",
                hmdbKategori = "Stokker for mobility og markering",
                hmdbBilde = "https://www.hjelpemiddeldatabasen.no/blobs/snet/36266.jpg?r=21112023143334",
                hmdbURL = "https://www.hjelpemiddeldatabasen.no/r11x.asp?linkinfo=36266&art0=74572&nart=1&pdisp=sh",
                kanByttes = true,
                kanByttesMedBrukerpass = true,
                artikkelStatus = "",
                innleveringsdato = null,
                utlånsType = "P",
                hmdbKategoriKortnavn = "",
                oppdatertInnleveringsdato = null,
                ordrenummer = "123",
            ),
        )

        /*
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
         */
    }

    fun utlånPåIsokode(fnr: String, isokode: String): List<UtlånPåIsokode> {
        return emptyList()
        /*
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
                        datoUtsendelse = row.string("UTLÅNS_DATO"),
                    )
                }.asList,
            )
        }

        return items

         */
    }

    fun utlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        return null
        /*
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
                        utlånsDato = row.string("UTLÅNS_DATO"),
                    )
                }.asSingle,
            )
        }

        return item

         */
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
            val produkterNgMap = runCatching { HjelpemiddeldatabaseNgClient.hentProdukter(hmsnr) }.getOrElse { e ->
                log2.error(e) { "DEBUG GRUNNDATA: Exception while fetching hmdb-ng: $e" }
                listOf()
            }.groupBy { it.hmsArtNr!! }.mapValues { it.value.first() }

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
