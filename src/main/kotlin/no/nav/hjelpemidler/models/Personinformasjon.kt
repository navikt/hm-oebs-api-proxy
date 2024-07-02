package no.nav.hjelpemidler.models

data class Personinformasjon(
    val brukerNr: String,
    val bostedsadresse: Adresse,
    val leveringsadresse: Adresse,
    val bydel: String?,
    val primaerAdr: String,
    val aktiv: Boolean,
) {
    @Deprecated("Bruk leveringsadresse")
    val leveringAddresse: String get() = leveringsadresse.adresse

    @Deprecated("Bruk leveringsadresse")
    val leveringPostnr: String get() = leveringsadresse.postnummer

    @Deprecated("Bruk leveringsadresse")
    val leveringBy: String get() = leveringsadresse.poststed

    @Deprecated("Bruk leveringsadresse")
    val leveringKommune: String get() = leveringsadresse.kommune
}
