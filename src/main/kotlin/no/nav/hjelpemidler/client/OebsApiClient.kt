package no.nav.hjelpemidler.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.hjelpemidler.Configuration
import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.logging
import no.nav.hjelpemidler.models.BestillingsordreRequest
import no.nav.hjelpemidler.models.OebsJsonFormat
import no.nav.hjelpemidler.models.Ordre
import no.nav.hjelpemidler.models.OrdreType
import no.nav.hjelpemidler.serialization.jackson.jsonMapper
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.ssl.SSLContextBuilder

private val log = KotlinLogging.logger {}

class OebsApiClient(engine: HttpClientEngine) {
    private val client = createHttpClient(
        if (Environment.current.isDev) {
            Apache5.create {
                // Trust all certificates
                sslContext = SSLContextBuilder()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build()

                configureConnectionManager {
                    setTlsStrategy(
                        DefaultClientTlsStrategy(
                            this@create.sslContext,
                            // Disable hostname verification (e.g. mismatched CN/SAN)
                            NoopHostnameVerifier.INSTANCE,
                        ),
                    )
                }
            }
        } else {
            engine
        },
    ) {
        if (!Environment.current.isProd) {
            logging {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Basic $apiToken")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    private val apiUrl = Configuration.OEBS_API_URL
    private val apiOrdreEndpoint = "${apiUrl.trimEnd('/')}/webservices/rest/opprettordre/digihotordreontinfo/"
    private val apiToken = Configuration.OEBS_API_TOKEN

    suspend fun opprettOrdre(request: BestillingsordreRequest): String {
        val bestilling =
            Ordre(
                fodselsnummer = request.fodselsnummer,
                formidlernavn = request.formidlernavn,
                ordretype = OrdreType.BESTILLING,
                saksnummer = request.saksnummer,
                artikler = request.artikler.map(Ordre::Artikkel),
                shippinginstructions = request.shippinginstructions,
                ferdigstill = request.ferdigstillOrdre.toString(),
            )
        log.info { "Kaller OeBS-API, url: $apiUrl" }
        val response = httpPostRequest(bestilling)
        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.body<Map<String, Map<String, String>>>()
            log.info { "Fikk svar fra OeBS: $responseBody" }
            return "Ordreopprettelse sendt til OeBS: ${responseBody["OutputParameters"]?.get("P_RETUR_MELDING")}"
        }

        val responseBody = runCatching { response.bodyAsText() }.getOrNull()
        error(
            "Feil under kall til OeBS-API, status: ${response.status}, body: $responseBody",
        )
    }

    suspend fun ping(): Boolean {
        val apiUrl404 = apiOrdreEndpoint.replace("digihotordreontinfo", "digihotordreontinf")

        data class ISGServiceFault(
            @JsonProperty("Code")
            val code: String,
            @JsonProperty("Message")
            val message: String,
            @JsonProperty("Resolution")
            val resolution: String,
        )

        data class Response(
            @JsonProperty("ISGServiceFault")
            val isgServiceFault: ISGServiceFault,
        )

        runCatching {
            val response = client.post(apiUrl404) {
                setBody(OebsJsonFormat("Test/ping melding!"))
            }

            val responseBody = runCatching { response.bodyAsText() }.getOrNull() ?: "{}"
            val success = runCatching {
                jsonMapper.readValue<Response>(responseBody)
            }.getOrNull()?.isgServiceFault?.code == "ISG_INVALID_FUNCTION"

            log.info { "Ping mot OeBS API-et (kall mot ugyldig uri) resultat -  status: ${response.status}, success: $success, body: $responseBody" }

            return success
        }.onFailure { e ->
            log.error(e) { "Exception oppstod mens vi kjørte ping mot OeBS API-et" }
        }

        return false
    }

    private suspend fun httpPostRequest(
        bestilling: Ordre,
    ): HttpResponse = client.post(apiOrdreEndpoint) {
        setBody(InputParameters(OebsJsonFormat(bestilling)))
    }

    data class InputParameters(
        @JsonProperty("InputParameters")
        val inputParameters: OebsJsonFormat,
    )
}
