package no.nav.hjelpemidler.client

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.models.BestillingsordreRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class OebsApiClientTest {
    @Test
    fun `Oppretter ordre i OEBS`() {
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
                BestillingsordreRequest(
                    fodselsnummer = "15084300133",
                    formidlernavn = "SEDAT KRONJUVEL",
                    saksnummer = "1",
                    artikler = listOf(BestillingsordreRequest.Artikkel(hmsnr = "1", antall = "1")),
                ),
            )
        }

        assertEquals("Ordreopprettelse sendt til OEBS: foobar", result)
    }
}
