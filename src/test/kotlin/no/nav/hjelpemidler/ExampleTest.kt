package no.nav.hjelpemidler

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ExampleTest {
    @Test
    fun `Example test`() {
        assertEquals(22, 22)
    }

    /*@Test
    fun `Parsing av hjelpemiddeldatabasen`() {
        assertEquals(22, 22)

        val apiURL = Configuration.application["HJELPEMIDDELDATABASEN_API"]!!

        val client = HttpClient.newBuilder().build()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiURL + "/produkter/alle-aktive-med-nav-avtale"))
            .timeout(Duration.ofSeconds(30))
            .header("Accepts", "application/json")
            .GET()
            .build()

        val response: HttpResponse<String> = client.send(request, BodyHandlers.ofString())
        System.out.println(response.statusCode())
        System.out.println(response.body())


        val db: List<HjelpemiddelProdukt> = mapperJson.readValue(response.body().toString())
        val p = findByHMS(db, "177946")
        System.out.printf("Found product: %s.\n", mapperJson.writeValueAsString(p))
    }

    fun findByHMS(db: List<HjelpemiddelProdukt>, hmsNr: String): HjelpemiddelProdukt? {
        db.forEach {
            if (it.stockid == hmsNr) {
                return it
            }
        }
        return null
    }*/
}
