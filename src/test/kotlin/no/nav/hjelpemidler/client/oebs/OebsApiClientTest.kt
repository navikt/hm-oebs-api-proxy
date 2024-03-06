package no.nav.hjelpemidler.client.oebs

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.models.BestillingsOrdreRequest
import no.nav.hjelpemidler.models.OrdreArtikkel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OebsApiClientTest {

    @Test
    internal fun `oppretter ordre i oebs`() {
        val engine = MockEngine {
            respond(
                """{ "OutputParameters": { "P_RETUR_MELDING": "foobar" } }""",
                HttpStatusCode.OK,
                headersOf("Content-Type", "application/json"),
            )
        }

        val client = OebsApiClient(engine)

        val result = runBlocking {
            client.opprettOrdre(
                BestillingsOrdreRequest(
                    fodselsnummer = "15084300133",
                    formidlernavn = "SEDAT KRONJUVEL",
                    saksnummer = "1",
                    artikler = listOf(OrdreArtikkel(hmsnr = "1", antall = "1")),
                ),
            )
        }

        assertEquals("Ordreopprettelse sendt til OEBS: foobar", result)
    }
}
