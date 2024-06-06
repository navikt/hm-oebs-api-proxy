package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText

data class Fødselsnummer(@JsonValue val value: String) {
    init {
        require(elevenDigits.matches(value)) {
            "$value er ikke et gyldig fødselsnummer"
        }
    }
}

private val elevenDigits = Regex("\\d{11}")

suspend fun ApplicationCall.receiveFødselsnummer(): Fødselsnummer = receiveText().let(::Fødselsnummer)
