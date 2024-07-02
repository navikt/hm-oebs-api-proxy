package no.nav.hjelpemidler.models

import no.nav.hjelpemidler.database.Row

data class Adresse(
    val adresse: String,
    val postnummer: String,
    val poststed: String,
    val kommunenummer: String,
)

fun Row.adresse(prefix: String = ""): Adresse = Adresse(
    adresse = stringOrNull("${prefix}_adresse") ?: "",
    postnummer = stringOrNull("${prefix}_postnummer") ?: "",
    poststed = stringOrNull("${prefix}_poststed") ?: "",
    kommunenummer = stringOrNull("${prefix}_kommune") ?: "",
)
