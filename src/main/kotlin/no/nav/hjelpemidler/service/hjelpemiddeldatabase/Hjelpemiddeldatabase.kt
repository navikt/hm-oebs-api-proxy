package no.nav.hjelpemidler.service.hjelpemiddeldatabase

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.HjelpemiddelProdukt
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val mapperJson = jacksonObjectMapper().registerModule(JavaTimeModule())

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

class Hjelpemiddeldatabase {
    companion object {
        private var database: List<HjelpemiddelProdukt>? = null

        fun loadDatabase() {
            if (database == null) {
                val apiURL = "${Configuration.application["HJELPEMIDDELDATABASEN_API"]!!}/produkter/alle-aktive-med-nav-avtale"
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accepts", "application/json")
                    .GET()
                    .build()
                val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 200) {
                    database = mapperJson.readValue(response.body().toString())
                }else{
                    logg.error("Unable to download the hjelpemiddel-database: statusCode=${response.statusCode()} url=$apiURL")
                }
            }
        }

        fun findByHmsNr(hmsNr: String): HjelpemiddelProdukt? {
            if (database == null) return null
            database!!.forEach {
                if (it.stockid == hmsNr) {
                    return it
                }
            }
            return null
        }
    }
}