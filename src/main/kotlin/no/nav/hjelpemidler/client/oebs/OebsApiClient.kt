package no.nav.hjelpemidler.client.oebs

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Artikkel
import no.nav.hjelpemidler.models.BestillingsOrdreRequest
import no.nav.hjelpemidler.models.Ordre
import no.nav.hjelpemidler.models.OrdreType
import org.slf4j.LoggerFactory

class OebsApiClient(engine: HttpClientEngine) {
    private val log = LoggerFactory.getLogger("OebsApiClient")
    private val client = HttpClient(engine = engine) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    private val apiUrl = Configuration.oebsApi.getValue("OEBS_API_URL")
    private val apiToken = Configuration.oebsApi.getValue("OEBS_API_TOKEN")

    suspend fun opprettOrdre(request: BestillingsOrdreRequest): String {

        val bestilling = Ordre(
            fodselsnummer = request.fodselsnummer,
            formidlernavn = request.formidlernavn,
            ordretype = OrdreType.BESTILLING,
            saksnummer = request.saksnummer,
            artikler = request.artikler.map { Artikkel(hmsnr = it.hmsnr, antall = it.antall) }
        )

        log.info("Kaller oebs api $bestilling")
        val response = httpPostRequest(bestilling)
        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.body<Map<String, Map<String, String>>>()
            log.info("Fikk svar fra oebs: $responseBody")
            return "Ordreopprettelse sendt til OEBS: ${responseBody["OutputParameters"]?.get("P_RETUR_MELDING")}"
        }

        val responseBody = response.bodyAsText()
        throw RuntimeException(
            "Error when calling OEBS API. Got Http response code ${response.status}: $responseBody"
        )
    }

    private suspend fun httpPostRequest(
        bestilling: Ordre,
    ): HttpResponse {
        return client.post(apiUrl) {
            headers {
                contentType(ContentType.Application.Json)
                append(HttpHeaders.Accept, "application/json")
                bearerAuth(apiToken)
            }
            setBody(bestilling)
        }
    }
}
