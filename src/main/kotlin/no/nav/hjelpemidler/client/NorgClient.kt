package no.nav.hjelpemidler.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.Configuration
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.logging

private val log = KotlinLogging.logger {}

class NorgClient(engine: HttpClientEngine = CIO.create()) {
    private val client = createHttpClient(engine) {
        if (!Environment.current.isProd) {
            logging {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    private val apiUrl = Configuration.NORG_API_URL

    internal suspend fun hentArbeidsfordelingenheter(kommunenummer: String): List<ArbeidsfordelingEnhet> {
        try {
            val url = "$apiUrl/arbeidsfordeling/enheter/bestmatch"
            log.info { "Henter arbeidsfordelingenhet med url: '$url' for kommunenummer $kommunenummer" }

            return withContext(Dispatchers.IO) {
                client.post(url) {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "geografiskOmraade" to kommunenummer,
                            "tema" to "HJE",
                            "temagruppe" to "HJLPM",
                        ),
                    )
                }.body()
            }
        } catch(e: Exception) {
            log.error(e) { "Klarte ikke hente arbeidsfordelingenheter for kommunenummer $kommunenummer" }
            throw e
        }
    }
}

data class ArbeidsfordelingEnhet(
    val navn: String,
    val enhetNr: String,
    val type: String,
)
