package no.nav.hjelpemidler.client

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.Configuration
import no.nav.hjelpemidler.client.hmdb.HentProdukter
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import java.net.URI

object GrunndataClient {
    private val log = KotlinLogging.logger {}
    private val client = GraphQLKtorClient(url = URI("${Configuration.GRUNNDATA_API_URL}/graphql").toURL())

    suspend fun hentProdukter(hmsnrs: Set<String>): List<Product> {
        if (hmsnrs.isEmpty()) return emptyList()
        val request = HentProdukter(variables = HentProdukter.Variables(hmsnrs = hmsnrs.toList()))
        return try {
            val response = client.execute(request)
            when {
                response.errors != null -> {
                    log.error { "Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs: $hmsnrs, errors: '${response.errors?.map { it.message }}'" }
                    emptyList()
                }

                response.data != null -> response.data?.products ?: emptyList()
                else -> emptyList()
            }
        } catch (e: Exception) {
            log.error(e) { "Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs: $hmsnrs" }
            return emptyList()
        }
    }
}
