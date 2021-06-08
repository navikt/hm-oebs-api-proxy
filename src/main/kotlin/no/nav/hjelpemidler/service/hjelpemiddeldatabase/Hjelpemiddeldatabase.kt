package no.nav.hjelpemidler.service.hjelpemiddeldatabase

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.HjelpemiddelProdukt
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val mapperJson = jacksonObjectMapper().registerModule(JavaTimeModule())

class Hjelpemiddeldatabase {
    companion object {
        private var database: List<HjelpemiddelProdukt>? = null

        fun loadDatabase() {
            if (database == null) {
                val apiURL = Configuration.application["HJELPEMIDDELDATABASEN_API"]!!
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL + "/produkter/alle-aktive-med-nav-avtale"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accepts", "application/json")
                    .GET()
                    .build()
                val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
                database = mapperJson.readValue(response.body().toString())
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