package no.nav.hjelpemidler

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.maps.shouldContain
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.database.toMap
import no.nav.hjelpemidler.database.toQueryParameters
import no.nav.hjelpemidler.database.transactionAsync
import no.nav.hjelpemidler.models.Resultat
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.Stønadsklasse
import no.nav.hjelpemidler.serialization.jackson.jsonMapper
import kotlin.test.Test

class RoutesSaksbehandlingTest {
    @Test
    fun `Adresse finnes, tar med artikler`() = testApplication {
        val fnr = "12345678910"
        val artikkel = Serviceforespørsel.Artikkel("1", "1")

        opprettSF(fnr, artikkel)

        hentServiceforespørsler(fnr).shouldBeSingleton {
            it.shouldContain("JSON_ARTIKKELINFO_IN", jsonMapper.writeValueAsString(listOf(artikkel)))
        }
    }

    @Test
    fun `Adresse mangler, tar ikke med artikler`() = testApplication {
        val fnr = "01987654321"
        val artikkel = Serviceforespørsel.Artikkel("2", "1")

        opprettSF(fnr, artikkel)

        hentServiceforespørsler(fnr).shouldBeSingleton {
            it.shouldContain("JSON_ARTIKKELINFO_IN", null)
        }
    }

    @Test
    fun `Adresse ikke aktiv, tar ikke med artikler`() = testApplication {
        val fnr = "01011121314"
        val artikkel = Serviceforespørsel.Artikkel("3", "1")

        opprettSF(fnr, artikkel)

        hentServiceforespørsler(fnr).shouldBeSingleton {
            it.shouldContain("JSON_ARTIKKELINFO_IN", null)
        }
    }
}

private suspend fun TestContext.opprettSF(fnr: String, artikkel: Serviceforespørsel.Artikkel? = null) {
    client.post("/opprettSF") {
        contentType(ContentType.Application.Json)
        setBody(
            Serviceforespørsel(
                fødselsnummer = fnr,
                navn = "",
                stønadsklasse = Stønadsklasse.HJDAAN,
                resultat = Resultat.I,
                referansenummer = "",
                artikler = listOfNotNull(artikkel),
            ),
        )
    } shouldHaveStatus HttpStatusCode.Created
}

private suspend fun TestContext.hentServiceforespørsler(fnr: String): List<Map<String, Any?>> =
    transactionAsync(dataSource) {
        it.list(
            """
            SELECT * FROM apps.xxrtv_cs_digihot_sf_opprett
            WHERE fnr = :fnr
            """.trimIndent(),
            fnr.toQueryParameters("fnr"),
            Row::toMap,
        )
    }
