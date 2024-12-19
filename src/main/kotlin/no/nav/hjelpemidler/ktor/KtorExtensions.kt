package no.nav.hjelpemidler.ktor

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import no.nav.hjelpemidler.domain.person.Fødselsnummer

/**
 * Vi fjerner evt. "" i tilfelle [Fødselsnummer] sendes som JSON.
 */
suspend fun ApplicationCall.receiveFødselsnummer(): Fødselsnummer =
    receiveText().removeSurrounding("\"").let(::Fødselsnummer)

suspend fun ApplicationCall.permanentRedirect(url: String) {
    response.headers.append(HttpHeaders.Location, url)
    respond(HttpStatusCode.PermanentRedirect)
}
