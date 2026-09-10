package no.nav.hjelpemidler.models

data class UtlånForKommuneApi(
    val artikkelnr: String,
    val artikkelBeskrivelse: String,
    val serienr: String?,
    val antall: String,
    val antallEnhet: String,
    val isokategori: String,
    val datoUtsendelse: String?,
    val installasjonsAddresse: String,
    val installasjonsKommune: String,
    val installasjonsPostnummer: String,
    val installasjonsBy: String,
    val bostedsAddresse: String,
    val bostedsKommune: String,
    val bostedsPostnummer: String,
    val bostedsBy: String,
)
