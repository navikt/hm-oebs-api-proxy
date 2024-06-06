package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonValue

data class Fødselsnummer(@JsonValue val value: String) {
    init {
        require(elevenDigits.matches(value)) {
            "$value er ikke et gyldig fødselsnummer"
        }
    }
}

private val elevenDigits = Regex("\\d{11}")
