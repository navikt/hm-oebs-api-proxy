package no.nav.hjelpemidler.models

data class BestillingsordreRequest(
    val fodselsnummer: String,
    val formidlernavn: String,
    val saksnummer: String,
    val artikler: List<Artikkel>,
    val forsendelsesinfo: String? = null,
) {
    data class Artikkel(override val hmsnr: String, override val antall: String) : no.nav.hjelpemidler.models.Artikkel
}
