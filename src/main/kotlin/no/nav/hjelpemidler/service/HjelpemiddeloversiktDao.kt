package no.nav.hjelpemidler.service

import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.client.GrunndataClient
import no.nav.hjelpemidler.client.hmdb.enums.MediaType
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.sql.Sql
import no.nav.hjelpemidler.models.Utlån
import no.nav.hjelpemidler.models.UtlånMedProduktinfo

class HjelpemiddeloversiktDao(private val tx: JdbcOperations) {

    fun hentHjelpemiddeloversikt(fnr: String): List<UtlånMedProduktinfo> {
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
            UtlånMedProduktinfo(
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

        return berikOrdrelinjer(items)
    }

    fun utlånPåIsokode(fnr: String, isokode: String): List<UtlånPåIsokode> = tx.list(
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

    fun utlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? = tx.singleOrNull(
        """
            SELECT fnr, artikkelnummer, serie_nummer, utlåns_dato, opprettelsesdato, kategori3_nummer
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
            opprettetDato = row.string("opprettelsesdato"),
            isokode = row.string("kategori3_nummer"),
        )
    }

    fun utlånPåArtnr(artnr: String): List<Utlån> = tx.list(
        """
            SELECT fnr, artikkelnummer, serie_nummer, utlåns_dato, opprettelsesdato, kategori3_nummer
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE artikkelnummer = :artnr
        """.trimIndent(),
        mapOf("artnr" to artnr),
    ) { row ->
        Utlån(
            fnr = row.string("fnr"),
            artnr = row.string("artikkelnummer"),
            serienr = row.string("serie_nummer"),
            utlånsDato = row.string("utlåns_dato"),
            opprettetDato = row.stringOrNull("opprettelsesdato"),
            isokode = row.stringOrNull("kategori3_nummer"),
        )
    }

    data class UtlånPåIsokode(
        val kategoriNummer: String,
        val datoUtsendelse: String,
    )

    private fun berikOrdrelinjer(utlånListe: List<UtlånMedProduktinfo>): List<UtlånMedProduktinfo> = runBlocking {
        // Unique set of hmsnr to fetch data for
        val hmsnr = utlånListe.filter { it.artikkelNr.isNotEmpty() }.map { it.artikkelNr }.toSet()

        // Fetch data for hmsnr from hm-grunndata-api
        val produkter: List<Product> = GrunndataClient.hentProdukter(hmsnr)

        // Apply data to items
        val produkterByHmsnr = produkter.groupBy { it.hmsArtNr }
        utlånListe.map { utlån ->

            // Sorted by identifier (artid, like old grunndata-api), but still get the ACTIVE one if there are higher sorted INACTIVE ones.
            val produkt = produkterByHmsnr[utlån.artikkelNr]?.sortedBy { it.identifier }?.minByOrNull { it.status }

            if (produkt != null) {
                utlån.berikOrdrelinje(produkt)
            }

            utlån.berikBytteinfo()

            utlån
        }
    }

    private fun UtlånMedProduktinfo.berikOrdrelinje(produkt: Product) {
        hmdbBeriket = true
        hmdbProduktNavn = produkt.articleName
        hmdbBeskrivelse = produkt.attributes.text
        hmdbKategori = produkt.isoCategoryTitle
        hmdbBilde = produkt.media
            .filter { it.type == MediaType.IMAGE }
            .minByOrNull { it.priority }
            ?.uri?.let {
                "https://finnhjelpemiddel.nav.no/imageproxy/400d/$it"
            }
        hmdbURL = produkt.productVariantURL
        hmdbKategoriKortnavn = produkt.isoCategoryTitleShort
        hmdbIsoKategori = produkt.isoCategory
    }
}
