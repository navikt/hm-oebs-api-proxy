package no.nav.hjelpemidler.client.hmdbng

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import java.net.URI

object HjelpemiddeldatabaseNgClient {
    private val log = KotlinLogging.logger {}
    private val client =
        GraphQLKtorClient(
            url = URI("${Configuration.application["GRUNNDATA_API_NG_URL"]!!}/graphql").toURL(),
            httpClient = HttpClient(engineFactory = Apache),
            serializer = GraphQLClientJacksonSerializer(),
        )

    suspend fun hentProdukter(hmsnrs: Set<String>): List<no.nav.hjelpemidler.client.hmdbng.hentprodukter.Product> {
        if (hmsnrs.isEmpty()) return emptyList()
        val request = HentProdukter(variables = HentProdukter.Variables(hmsnrs = hmsnrs.toList()))
        return try {
            val response = client.execute(request)
            when {
                response.errors != null -> {
                    log.error("Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs=$hmsnrs, errors=${response.errors?.map { it.message }}")
                    emptyList()
                }

                response.data != null -> response.data?.products ?: emptyList()
                else -> emptyList()
            }
        } catch (e: Exception) {
            log.error("Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs=$hmsnrs", e)
            return emptyList()
        }
    }
}
