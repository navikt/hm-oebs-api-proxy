package no.nav.hjelpemidler.client.hmdbng

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import mu.KotlinLogging
import no.nav.hjelpemidler.Configuration
import no.nav.hjelpemidler.client.graphQLClientSerializer
import java.net.URI

object HjelpemiddeldatabaseNgClient {
    private val log = KotlinLogging.logger {}
    private val client = GraphQLKtorClient(
        url = URI("${Configuration.GRUNNDATA_API_NG_URL}/graphql").toURL(),
        serializer = graphQLClientSerializer,
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
