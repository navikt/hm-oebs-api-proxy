package no.nav.hjelpemidler.client.hmdb

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import mu.KotlinLogging
import no.nav.hjelpemidler.client.hmdb.hentproduktermedhmsnr.Produkt
import no.nav.hjelpemidler.configuration.Configuration
import java.net.URL

object HjelpemiddeldatabaseClient {
    private val logg = KotlinLogging.logger {}
    private val client =
        GraphQLKtorClient(
            url = URL("${Configuration.application["GRUNNDATA_API_URL"]!!}/graphql"),
            httpClient = HttpClient(engineFactory = Apache),
            serializer = GraphQLClientJacksonSerializer()
        )

    suspend fun hentProdukterMedHmsnr(hmsnr: String): List<Produkt> {
        val request = HentProdukterMedHmsnr(variables = HentProdukterMedHmsnr.Variables(hmsnr = hmsnr))
        return try {
            val response = client.execute(request)
            when {
                response.errors != null -> {
                    logg.error("Feil under henting av data fra hjelpemiddeldatabasen, hmsnr=$hmsnr, errors=${response.errors?.map { it.message }}")
                    emptyList()
                }
                response.data != null -> response.data?.produkter ?: emptyList()
                else -> emptyList()
            }
        } catch (e: Exception) {
            logg.error("Feil under henting av data fra hjelpemiddeldatabasen, hmsnr=$hmsnr", e)
            return emptyList()
        }
    }

    suspend fun hentProdukterMedHmsnrs(hmsnrs: Set<String>): List<no.nav.hjelpemidler.client.hmdb.hentproduktermedhmsnrs.Produkt> {
        if (hmsnrs.isEmpty()) return emptyList()
        val request = HentProdukterMedHmsnrs(variables = HentProdukterMedHmsnrs.Variables(hmsnrs = hmsnrs.toList()))
        return try {
            val response = client.execute(request)
            when {
                response.errors != null -> {
                    logg.error("Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs=$hmsnrs, errors=${response.errors?.map { it.message }}")
                    emptyList()
                }
                response.data != null -> response.data?.produkter ?: emptyList()
                else -> emptyList()
            }
        } catch (e: Exception) {
            logg.error("Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs=$hmsnrs", e)
            return emptyList()
        }
    }
}
