package no.nav.hjelpemidler

import io.ktor.client.HttpClient
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import no.nav.hjelpemidler.database.createTestDataSource
import no.nav.hjelpemidler.http.createHttpClient
import javax.sql.DataSource

fun testApplication(block: suspend TestContext.() -> Unit) {
    val dataSource = createTestDataSource()
    testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            installTestAuthentication()
            installRouting(dataSource)
        }

        TestContext(createHttpClient(client.engine), dataSource).block()
    }
}

class TestContext(val client: HttpClient, val dataSource: DataSource)
