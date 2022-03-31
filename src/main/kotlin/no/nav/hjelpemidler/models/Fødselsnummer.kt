package no.nav.hjelpemidler.models

data class Fødselsnummer(val value: String) {
    private val elevenDigits = Regex("\\d{11}")

    init {
        if (!elevenDigits.matches(value)) {
            throw IllegalArgumentException("$value er ikke gyldig fødselsnummer")
        }
    }
}