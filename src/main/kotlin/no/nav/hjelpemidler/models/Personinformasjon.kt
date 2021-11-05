package no.nav.hjelpemidler.models

data class Personinformasjon(
    val brukerNr: String,
    val leveringAddresse: String,
    val leveringKommune: String,
    val leveringPostnr: String,
    val leveringBy: String,
    val primaerAdr: String
)
