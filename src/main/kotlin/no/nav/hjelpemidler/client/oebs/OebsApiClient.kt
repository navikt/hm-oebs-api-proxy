package no.nav.hjelpemidler.client.oebs

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Artikkel
import no.nav.hjelpemidler.models.BestillingsOrdreRequest
import no.nav.hjelpemidler.models.OrdeType
import no.nav.hjelpemidler.models.Ordre
import org.slf4j.LoggerFactory

class OebsApiClient {
    private val log = LoggerFactory.getLogger("OebsApiClient")
    private val client = HttpClient(Apache) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    private val apiUrl = Configuration.oebsApi.getValue("OEBS_API_URL")
    private val apiToken = Configuration.oebsApi.getValue("OEBS_API_TOKEN")

    suspend fun opprettOrdre(request: BestillingsOrdreRequest): String {

        val bestilling = Ordre(
            fodselsnummer = request.fodselsnummer.value,
            formidlernavn = request.formidlernavn,
            ordretype = OrdeType.BESTILLING,
            saksnummer = request.saksnummer,
            artikler = request.artikler.map { Artikkel(hmsnr = it.hmsnr, antall = it.antall) }
        )

        val response = httpPostRequest(bestilling)
        val responseBody = response.body<Map<String, Map<String, String>>>()

        if (response.status != HttpStatusCode.OK) {
            throw RuntimeException(
                "Error when calling OEBS API. Got Http response code ${response.status}: ${
                responseBody.get("OutputParameters")?.get("P_RETUR_MELDING")
                }"
            )
        } else return "Ordreopprettelse sendt til OEBS: ${responseBody.get("OutputParameters")?.get("P_RETUR_MELDING")}"
    }

    private suspend fun httpPostRequest(
        bestilling: Ordre,
    ): HttpResponse {
        return client.post(apiUrl) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Authorization, "Bearer $apiToken")
            }
            setBody(bestilling)
        }
    }
}
