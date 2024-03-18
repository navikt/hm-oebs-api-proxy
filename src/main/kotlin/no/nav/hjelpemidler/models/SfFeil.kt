package no.nav.hjelpemidler.models

data class SfFeil(
    val id: String?,
    val saksnummer: String?,
    val referansenummer: String?,
    val processed: String?,
    val sfNummer: String?,
    val feilmelding: String?,
    val kommentar: String?,
    val creationDate: String?,
    val lastUpdateDate: String?,
)
