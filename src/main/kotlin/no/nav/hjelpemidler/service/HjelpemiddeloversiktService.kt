package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.client.GrunndataClient
import no.nav.hjelpemidler.client.hmdb.enums.MediaType
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import no.nav.hjelpemidler.collections.mapNotNullToSet
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.models.UtlånMedProduktinfo

class HjelpemiddeloversiktService(
    private val database: Database,
    private val grunndataClient: GrunndataClient,
) {
    suspend fun hentHjelpemiddeloversikt(fnr: String): List<UtlånMedProduktinfo> {
        val hjelpemiddeloversikt = database.transaction {
            hjelpemiddeloversiktDao.hentHjelpemiddeloversikt(fnr)
        }
        return berikOrdrelinjer(hjelpemiddeloversikt)
    }

    private suspend fun berikOrdrelinjer(hjelpemiddeloversikt: List<UtlånMedProduktinfo>): List<UtlånMedProduktinfo> {
        val hmsnr = hjelpemiddeloversikt
            .mapNotNullToSet { it.artikkelNr.takeUnless(String::isBlank) }
        val produkterByHmsnr = grunndataClient
            .hentProdukter(hmsnr)
            .groupBy(Product::hmsArtNr)
        return hjelpemiddeloversikt.map { utlån ->

            // Sorted by identifier (artid, like old grunndata-api), but still get the ACTIVE one if there are higher sorted INACTIVE ones.
            val produkt = produkterByHmsnr[utlån.artikkelNr]?.sortedBy { it.identifier }?.minByOrNull { it.status }

            if (produkt != null) {
                utlån.berikOrdrelinje(produkt)
            }

            utlån.berikBytteinfo()

            utlån
        }
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
