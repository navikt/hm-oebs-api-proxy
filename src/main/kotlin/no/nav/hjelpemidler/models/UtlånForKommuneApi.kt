package no.nav.hjelpemidler.models

import java.time.LocalDateTime

data class UtlånForKommuneApi(
    val artikkelnr: String,
    val artikkelBeskrivelse: String,
    val serienr: String?,
    val antall: String,
    val antallEnhet: String,
    val isokategori: String,
    val utlånsDato: LocalDateTime?,
    val installasjonAdresse: String?,
    val installasjonKommune: String?,
    val installasjonPostnummer: String?,
    val installasjonBy: String?,
    val bostedsAdresse: String?,
    val bostedsKommune: String?,
    val bostedsPostnummer: String?,
    val bostedsBy: String?,
)
