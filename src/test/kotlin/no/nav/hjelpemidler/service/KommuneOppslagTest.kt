package no.nav.hjelpemidler.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KommuneOppslagTest {
    private val kommuneOppslag: KommuneOppslag = KommuneOppslag()

    @Test
    fun `Skal kunne slå opp lager fra kommunenummer`() {
        kommuneOppslag.hentOrgNavn("1134") shouldBe "*11 Rogaland"
    }

    @Test
    fun `Ugyldig kommunenummer skal ikke kaste exception`() {
        shouldNotThrowAny { kommuneOppslag.hentOrgNavn("0000") }
    }
}
