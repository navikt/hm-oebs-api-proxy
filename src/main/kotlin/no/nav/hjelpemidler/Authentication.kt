package no.nav.hjelpemidler

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector
import java.net.URL
import java.util.concurrent.TimeUnit

private val logg = KotlinLogging.logger {}

private fun httpClientWithProxy() = HttpClient(Apache) {
    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
    engine {
        customizeClient {
            setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
        }
    }
}

fun Application.installAuthentication() {
    // Load token X config for rest client authentication
    var tokenXConfig: WellKnownConfig?
    logg.info("DEBUG HERE2.1: tokenXWellKnownUrl=${Configuration.tokenX["TOKEN_X_WELL_KNOWN_URL"]!!}, clientId=${Configuration.tokenX["TOKEN_X_CLIENT_ID"]!!}")
    runBlocking {
        tokenXConfig = WellKnownConfig(
            metadata = httpClientWithProxy().get(Configuration.tokenX["TOKEN_X_WELL_KNOWN_URL"]!!).body(),
            clientId = Configuration.tokenX["TOKEN_X_CLIENT_ID"]!!
        )
    }
    logg.info("DEBUG HERE2.2")

    val jwkProviderTokenX = JwkProviderBuilder(URL(tokenXConfig!!.metadata.jwksUri))
        // cache up to 10 JWKs for 24 hours
        .cached(10, 24, TimeUnit.HOURS)
        // if not cached, only allow max 10 different keys per minute to be fetched from external provider
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
    logg.info("DEBUG HERE2.3: azure_well_known_url=${Configuration.azureAD["AZURE_APP_WELL_KNOWN_URL"]!!}, azure_client_id=${Configuration.azureAD["AZURE_APP_CLIENT_ID"]!!}")

    // Load Azure AD config for rest client authentication
    var aadConfig: WellKnownConfig?
    runBlocking {
        aadConfig = WellKnownConfig(
            metadata = httpClientWithProxy().get(Configuration.azureAD["AZURE_APP_WELL_KNOWN_URL"]!!).body(),
            clientId = Configuration.azureAD["AZURE_APP_CLIENT_ID"]!!
        )
    }
    logg.info("DEBUG HERE2.4")

    val jwkProviderAad = JwkProviderBuilder(URL(aadConfig!!.metadata.jwksUri))
        // cache up to 10 JWKs for 24 hours
        .cached(10, 24, TimeUnit.HOURS)
        // if not cached, only allow max 10 different keys per minute to be fetched from external provider
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
    logg.info("DEBUG HERE2.5")

    // Install auth providers
    install(Authentication) {
        jwt("tokenX") {
            verifier(jwkProviderTokenX, tokenXConfig!!.metadata.issuer)
            validate { credentials ->
                try {
                    requireNotNull(credentials.payload.audience) {
                        "Auth: Missing audience in token"
                    }
                    require(credentials.payload.audience.contains(tokenXConfig!!.clientId)) {
                        "Auth: Valid audience not found in claims"
                    }
                    JWTPrincipal(credentials.payload)
                } catch (e: Throwable) {
                    null
                }
            }
        }
        jwt("aad") {
            verifier(jwkProviderAad, aadConfig!!.metadata.issuer)
            validate { credentials ->
                try {
                    requireNotNull(credentials.payload.audience) {
                        "Auth: Missing audience in token"
                    }
                    require(credentials.payload.audience.contains(aadConfig!!.clientId)) {
                        "Auth: Valid audience not found in claims"
                    }
                    JWTPrincipal(credentials.payload)
                } catch (e: Throwable) {
                    null
                }
            }
        }
    }

    logg.info("DEBUG HERE2.6")
}

private data class WellKnownConfig(
    val metadata: Metadata,
    val clientId: String
) {
    data class Metadata(
        @JsonProperty("issuer") val issuer: String,
        @JsonProperty("jwks_uri") val jwksUri: String
    )
}
