package no.nav.hjelpemidler

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import no.nav.hjelpemidler.http.openid.AzureADEnvironmentVariable
import no.nav.hjelpemidler.http.openid.TokenXEnvironmentVariable
import java.net.URI
import java.util.concurrent.TimeUnit

fun Application.installAuthentication() {
    val jwkProviderTokenX = jwkProvider(TokenXEnvironmentVariable.TOKEN_X_JWKS_URI)
    val jwkProviderAad = jwkProvider(AzureADEnvironmentVariable.AZURE_OPENID_CONFIG_JWKS_URI)

    install(Authentication) {
        jwt("tokenX") {
            verifier(jwkProviderTokenX, TokenXEnvironmentVariable.TOKEN_X_ISSUER) {
                withAudience(TokenXEnvironmentVariable.TOKEN_X_CLIENT_ID)
            }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
        jwt("aad") {
            verifier(jwkProviderAad, AzureADEnvironmentVariable.AZURE_OPENID_CONFIG_ISSUER) {
                withAudience(AzureADEnvironmentVariable.AZURE_APP_CLIENT_ID)
            }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }
}

private fun jwkProvider(urlString: String): JwkProvider = JwkProviderBuilder(URI(urlString).toURL())
    // cache up to 10 JWKs for 24 hours
    .cached(10, 24, TimeUnit.HOURS)
    // if not cached, only allow max 10 different keys per minute to be fetched from external provider
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build()
