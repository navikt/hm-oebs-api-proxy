package no.nav.hjelpemidler.models

data class ServiceforespørselRequest(
    val fødselsnummer: String,
    val navn: String,
    val stønadsklasse: Stønadsklasse,
    val resultat: Resultat,
    val referansenummer: String,
    val problemsammendrag: String? = null,
    val forsendelsesinfo: String? = null,
    val artikler: List<SfArtikkel>? = null,
    val notat: String? = null,
)
