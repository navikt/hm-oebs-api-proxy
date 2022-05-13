package no.nav.hjelpemidler.models

data class BestillingsOrdreRequest(
    val fodselsnummer: Fødselsnummer,
    val formidlernavn: String,
    val saksnummer: String,
    val artikler: List<OrdreArtikkel>
)

data class OrdreArtikkel(val hmsnr: String, val antall: String)
