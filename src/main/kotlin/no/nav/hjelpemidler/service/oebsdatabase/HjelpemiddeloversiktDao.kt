package no.nav.hjelpemidler.service.oebsdatabase

import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.client.hmdb.HjelpemiddeldatabaseClient
import no.nav.hjelpemidler.client.hmdb.hentproduktermedhmsnrs.Produkt
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.HjelpemiddelBruker
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class HjelpemiddeloversiktDao(private val dataSource: DataSource = Configuration.dataSource) {

    fun hentHjelpemiddeloversikt(fnr: String): List<HjelpemiddelBruker> {
        @Language("OracleSQL")
        val query =
            """
                SELECT ANTALL, ENHET, KATEGORI3_BESKRIVELSE, ARTIKKEL_BESKRIVELSE, ARTIKKELNUMMER, SERIE_NUMMER, FØRSTE_UTSENDELSE
                FROM XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
                WHERE FNR = ?
                ORDER BY FØRSTE_UTSENDELSE DESC
            """.trimIndent()

        val items = sessionOf(dataSource).use {
            it.run(
                queryOf(query, fnr).map { row ->
                    HjelpemiddelBruker(
                        antall = row.string("ANTALL"),
                        antallEnhet = row.string("ENHET"),
                        kategori = row.string("KATEGORI3_BESKRIVELSE"),
                        artikkelBeskrivelse = row.string("ARTIKKEL_BESKRIVELSE"),
                        artikkelNr = row.string("ARTIKKELNUMMER"),
                        serieNr = row.stringOrNull("SERIE_NUMMER"),
                        datoUtsendelse = row.string("FØRSTE_UTSENDELSE"),
                    )
                }.asList
            )
        }
        return berikOrdrelinjer(items)
    }

    private fun berikOrdrelinjer(items: List<HjelpemiddelBruker>): List<HjelpemiddelBruker> = runBlocking {
        // Unique list of hmsnrs to fetch data for
        val hmsNrs = items.filter { it.artikkelNr.isNotEmpty() }.map { it.artikkelNr }.toSet()

        // Fetch data for hmsnrs from hm-grunndata-api
        val produkter: List<Produkt> = HjelpemiddeldatabaseClient.hentProdukterMedHmsnrs(hmsNrs)

        // Apply data to items
        val produkterByHmsnr = produkter.groupBy { it.hmsnr }
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

            if (produkt.produktId != null && produkt.artikkelId != null) {
                item.hmdbURL =
                    "https://www.hjelpemiddeldatabasen.no/r11x.asp?linkinfo=${produkt.produktId}&art0=${produkt.artikkelId}&nart=1"
            }
        }
        return item
    }
}
