package no.nav.hjelpemidler.models

data class BestillingsOrdreRequest(
    val fodselsnummer: String,
    val formidlernavn: String,
    val saksnummer: String,
    val artikler: List<OrdreArtikkel>
)

data class OrdreArtikkel(val hmsnr: String, val antall: String)
