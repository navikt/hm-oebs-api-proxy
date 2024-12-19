package no.nav.hjelpemidler.ktor

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import no.nav.hjelpemidler.domain.person.Fødselsnummer

/**
 * Vi fjerner evt. " i tilfelle [Fødselsnummer] sendes som JSON.
 */
suspend fun ApplicationCall.receiveFødselsnummer(): Fødselsnummer =
    receiveText().removeSurrounding("\"").let(::Fødselsnummer)
