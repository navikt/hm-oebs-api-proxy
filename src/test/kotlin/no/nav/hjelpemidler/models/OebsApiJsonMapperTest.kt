package no.nav.hjelpemidler.models

import kotlin.test.Test
import kotlin.test.assertEquals

class OebsApiJsonMapperTest {
    @Test
    fun `Mapper ordre til OEBS sitt nøstede JSON-format`() {
        val oebsJsonFormat = OebsJsonFormat(
            Ordre(
                "04331234565",
                "Formidler Navn",
                saksnummer = "1337",
                artikler = listOf(
                    Ordre.Artikkel("1111", "1"),
                    Ordre.Artikkel("2222", "2"),
                ),
                shippinginstructions = "Skal til XK-lager",
                ferdigstill = true,
            ),
        )

        assertEquals(
            "{\"fodselsnummer\":\"04331234565\",\"formidlernavn\":\"Formidler Navn\",\"ordretype\":\"BESTILLING\",\"saksnummer\":\"1337\",\"artikler\":[{\"hmsnr\":\"1111\",\"antall\":\"1\"},{\"hmsnr\":\"2222\",\"antall\":\"2\"}],\"shippinginstructions\":\"Skal til XK-lager\",\"ferdigstill\":true}",
            oebsJsonFormat.jsonMelding,
        )
    }
}
