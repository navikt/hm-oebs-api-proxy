package no.nav.hjelpemidler

import no.nav.hjelpemidler.lagerstatus.KommuneOppslag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

internal class KommuneOppslagTest {
    val kommuneOppslag: KommuneOppslag = KommuneOppslag()

    @Test
    fun `skal kunne slå opp lager fra kommunenummr`() {
        val kommunenummer = "1134"
        val lagerRogaland = "*11 Rogaland"
        val ret = kommuneOppslag.hentLagerKode(kommunenummer)
        assertEquals(ret, lagerRogaland)
    }

    @Test
    fun `ugyldig kommunenummer skal ikke throwe exception`() {
        val kommunenummer = "0000"
        assertDoesNotThrow { kommuneOppslag.hentLagerKode(kommunenummer) }
    }
}
