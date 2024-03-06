package no.nav.hjelpemidler

import no.nav.hjelpemidler.models.Artikkel
import no.nav.hjelpemidler.models.OebsJsonFormat
import no.nav.hjelpemidler.models.Ordre
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class OebsApiJsonMapperTest {

    @Test
    fun `mapper ordre til Oebs sitt nøsta Json format`() {
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
