package no.nav.hjelpemidler.client

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import no.nav.hjelpemidler.Configuration
import no.nav.hjelpemidler.client.hmdb.HentProdukter
import no.nav.hjelpemidler.client.hmdb.hentprodukter.Product
import java.io.Closeable
import java.net.URI

private val log = KotlinLogging.logger {}

class GrunndataClient(engine: HttpClientEngine) : Closeable {
    private val client = GraphQLKtorClient(
        url = URI("${Configuration.GRUNNDATA_API_URL}/graphql").toURL(),
        httpClient = HttpClient(engine),
    )

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
            if (e is CancellationException) {
                currentCoroutineContext().ensureActive()
            }
            log.error(e) { "Feil under henting av data fra hjelpemiddeldatabasen, hmsnrs: $hmsnrs" }
            return emptyList()
        }
    }

    override fun close() {
        client.close()
    }
}
