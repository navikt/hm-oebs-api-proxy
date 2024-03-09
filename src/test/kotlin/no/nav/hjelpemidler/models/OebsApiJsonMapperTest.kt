package no.nav.hjelpemidler.models

import kotlin.test.Test
import kotlin.test.assertEquals

class OebsApiJsonMapperTest {
    @Test
    fun `Mapper ordre til Oebs sitt nøsta Json format`() {
        val oebsJsonFormat = OebsJsonFormat(
            Ordre(
                "04331234565",
                "Navn navnesen",
                saksnummer = "1234",
                artikler = listOf(
                    Artikkel("1111", "1"),
                    Artikkel("2222", "2"),
                ),
                shippinginstructions = "Skal til XK-lager",
            ),
        )

        assertEquals(
            "{\"fodselsnummer\":\"04331234565\",\"formidlernavn\":\"Navn navnesen\",\"ordretype\":\"BESTILLING\",\"saksnummer\":\"1234\",\"artikler\":[{\"hmsnr\":\"1111\",\"antall\":\"1\"},{\"hmsnr\":\"2222\",\"antall\":\"2\"}],\"shippinginstructions\":\"Skal til XK-lager\"}",
            oebsJsonFormat.jsonMelding,
        )
    }
}
