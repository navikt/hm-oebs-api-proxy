package no.nav.hjelpemidler.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
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
import no.nav.hjelpemidler.isNotProd
import no.nav.hjelpemidler.jsonMapper
import no.nav.hjelpemidler.models.BestillingsordreRequest
import no.nav.hjelpemidler.models.OebsJsonFormat
import no.nav.hjelpemidler.models.Ordre
import no.nav.hjelpemidler.models.OrdreType

private val log = KotlinLogging.logger {}

class OebsApiClient(engine: HttpClientEngine) {
    private val client = createHttpClient(engine, jsonMapper) {
        if (isNotProd()) {
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
    private val apiToken = Configuration.OEBS_API_TOKEN

    suspend fun opprettOrdre(request: BestillingsordreRequest): String {
        val bestilling = when {
            Environment.current.tier.isProd -> Ordre(
                fodselsnummer = request.fodselsnummer,
                formidlernavn = request.formidlernavn,
                ordretype = OrdreType.BESTILLING,
                saksnummer = request.saksnummer,
                artikler = request.artikler.map { Ordre.Artikkel(hmsnr = it.hmsnr, antall = it.antall) },
                shippinginstructions = when {
                    request.forsendelsesinfo.isNullOrBlank() -> request.formidlernavn
                    else -> request.forsendelsesinfo
                },
            )
            else -> Ordre(
                fodselsnummer = request.fodselsnummer,
                formidlernavn = request.formidlernavn,
                ordretype = OrdreType.BESTILLING,
                saksnummer = request.saksnummer,
                artikler = request.artikler.map { Ordre.Artikkel(hmsnr = it.hmsnr, antall = it.antall) },
                shippinginstructions = when {
                    request.forsendelsesinfo.isNullOrBlank() -> request.formidlernavn
                    else -> request.forsendelsesinfo
                },
                // ferdigstill = request.ferdigstillOrdre.toString(),
            )
        }
        log.info { "Kaller OEBS-API, url: $apiUrl" }
        val response = httpPostRequest(bestilling)
        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.body<Map<String, Map<String, String>>>()
            log.info { "Fikk svar fra OEBS: $responseBody" }
            return "Ordreopprettelse sendt til OEBS: ${responseBody["OutputParameters"]?.get("P_RETUR_MELDING")}"
        }

        val responseBody = runCatching { response.bodyAsText() }.getOrNull()
        error(
            "Feil under kall til OEBS-API, status: ${response.status}, body: $responseBody",
        )
    }

    private suspend fun httpPostRequest(
        bestilling: Ordre,
    ): HttpResponse = client.post(apiUrl) {
        setBody(OebsJsonFormat(bestilling))
    }
}
