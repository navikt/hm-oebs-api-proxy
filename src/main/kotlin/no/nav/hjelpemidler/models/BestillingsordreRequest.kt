package no.nav.hjelpemidler.models

data class BestillingsordreRequest(
    val fodselsnummer: String,
    val formidlernavn: String,
    val saksnummer: String,
    val artikler: List<OrdreArtikkel>,
    val forsendelsesinfo: String? = null,
)

data class OrdreArtikkel(val hmsnr: String, val antall: String)
