package no.nav.hjelpemidler

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import no.nav.hjelpemidler.client.GrunndataClient
import no.nav.hjelpemidler.client.OebsApiClient
import no.nav.hjelpemidler.database.Database
import no.nav.hjelpemidler.database.createTestDataSource
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.år
import no.nav.hjelpemidler.domain.tilgang.NavIdent
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.Texas
import no.nav.hjelpemidler.http.openid.TokenSet
import no.nav.hjelpemidler.http.openid.asTexas
import no.nav.hjelpemidler.norg.NorgClient
import no.nav.hjelpemidler.server.ktor.security.AutentisertAnsatt
import no.nav.hjelpemidler.server.ktor.security.AutentisertInnbygger
import no.nav.hjelpemidler.server.ktor.security.AuthenticationProviderName
import no.nav.hjelpemidler.server.ktor.security.IdPortenSecurityLevel
import no.nav.hjelpemidler.service.HjelpemiddeloversiktService
import no.nav.hjelpemidler.service.NorgService
import javax.sql.DataSource
import kotlin.time.Duration.Companion.hours

fun testApplication(block: suspend TestContext.() -> Unit) {
    val dataSource = createTestDataSource()

    testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            dependencies {
                provide<HttpClientEngine> { this@testApplication.client.engine }
                provide<Texas> { TokenSet("testToken", 1.hours).asTexas() }

                provide { dataSource }
                provide(::Database)

                provide { mockk<GrunndataClient>(relaxed = true) }
                provide { mockk<NorgClient>(relaxed = true) }
                provide { mockk<OebsApiClient>(relaxed = true) }

                provide(::HjelpemiddeloversiktService)
                provide(::NorgService)
            }

            install(Authentication) {
                provider(AuthenticationProviderName.ENTRA_ID) {
                    authenticate {
                        it.principal(
                            AutentisertAnsatt(
                                id = NavIdent.UKJENT,
                                navn = "test",
                                epost = "test@trygdeetaten.no",
                                grupper = emptySet(),
                                userToken = "",
                            ),
                        )
                    }
                }
                provider(AuthenticationProviderName.TOKEN_X) {
                    authenticate {
                        it.principal(
                            AutentisertInnbygger(
                                fnr = Fødselsnummer(40.år),
                                level = IdPortenSecurityLevel.HIGH,
                                userToken = "",
                            ),
                        )
                    }
                }
            }

            installRouting()
        }

        TestContext(createHttpClient(client.engine), dataSource).block()
    }
}

class TestContext(val client: HttpClient, val dataSource: DataSource)
