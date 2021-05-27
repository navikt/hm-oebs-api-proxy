package no.nav.hjelpemidler.models

data class HjelpemiddelBruker (
    val artikkelNr: String,
    val artikkelTittel: String,
    val artikkelKategori: String,
    val antall: Double,
)
