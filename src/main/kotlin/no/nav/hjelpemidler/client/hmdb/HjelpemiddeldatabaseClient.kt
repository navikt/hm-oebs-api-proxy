package no.nav.hjelpemidler.client.hmdb

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.client.hmdb.hentproduktermedhmsnr.Produkt
/*import no.nav.hjelpemidler.service.hjelpemiddeldatabase.HentProdukterMedHmsnr
import no.nav.hjelpemidler.service.hjelpemiddeldatabase.hentproduktermedhmsnr.Produkt*/
import java.net.URL

object HjelpemiddeldatabaseClient {
    private val logg = KotlinLogging.logger {}
    private val client =
        GraphQLKtorClient(
            // url = URL("${Configuration.application["GRUNNDATA_API_URL"]!!}/graphql"),
            url = URL("http://localhost:8880/graphql"),
            httpClient = HttpClient(engineFactory = Apache),
            serializer = GraphQLClientJacksonSerializer()
        )

    suspend fun hentProdukterMedHmsnr(hmsnr: String): List<Produkt> {
        val request = HentProdukterMedHmsnr(variables = HentProdukterMedHmsnr.Variables(hmsnr = hmsnr))
        return try {
            val response = client.execute(request)
            when {
                response.errors != null -> {
                    logg.warn("Feil under henting av data fra hjelpemiddeldatabasen, hmsnr=$hmsnr, errors=${response.errors?.map { it.message }}")
                    emptyList()
                }
                response.data != null -> response.data?.produkter ?: emptyList()
                else -> emptyList()
            }
        } catch (e: Exception) {
            logg.warn("Feil under henting av data fra hjelpemiddeldatabasen, hmsnr=$hmsnr", e)
            return emptyList()
        }
    }
}
