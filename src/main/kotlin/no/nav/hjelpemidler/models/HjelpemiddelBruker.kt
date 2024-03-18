package no.nav.hjelpemidler.models

data class HjelpemiddelBruker(
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
    var hmdbKategori: String? = null,
    var hmdbBilde: String? = null,
    var hmdbURL: String? = null,
    var hmdbKategoriKortnavn: String? = null,

    // Berikede felter for bytte
    var kanByttes: Boolean? = null,
    var kanByttesMedBrukerpass: Boolean? = null,
)
