package no.nav.hjelpemidler.models

data class UtlånMedProduktinfo(
    val antall: String,
    val antallEnhet: String,
    val kategoriNummer: String,
    val kategori: String,
    val artikkelBeskrivelse: String,
    val artikkelNr: String,
    val serieNr: String?,
    val datoUtsendelse: String?,
    val ordrenummer: String?,
    val artikkelStatus: String,
    val utlånsType: String?,
    val innleveringsdato: String?,
    val oppdatertInnleveringsdato: String?,
    var hmdbBeriket: Boolean = false,
    var hmdbProduktNavn: String? = null,
    var hmdbBeskrivelse: String? = null,
    var hmdbIsoKategori: String? = null, // ISO-kode
    var hmdbKategori: String? = null, // ISO-tittel
    var hmdbKategoriKortnavn: String? = null, // ISO-tittel kort
    var hmdbBilde: String? = null,
    var hmdbURL: String? = null,

    // Berikede felter for bytte
    var kanIkkeByttesGrunner: List<KanIkkeByttesGrunn>? = emptyList(),
    var kanByttes: Boolean? = null,
    var kanByttesMedBrukerpass: Boolean? = null,
)

enum class KanIkkeByttesGrunn {
    IKKE_RIKTIG_UTLÅNSTYPE,
    ER_FØRSTEGANGSUTLEVERT_MADRASS,
}

enum class Utlånstype(val kode: String) {
    PERMANENT("P"),
    TIDSBESTEMT_UTLÅN("F"),
    KORTTIDSUTLÅN("K"),
    UTPRØVINGSLÅN("U"),
}
