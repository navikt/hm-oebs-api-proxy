package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.database.OracleTestHelper
import no.nav.hjelpemidler.models.Fødselsnummer
import kotlin.test.Test

class BrukernummerDaoTest {
    private val brukernummerDao = BrukernummerDao(OracleTestHelper.dataSource)

    @Test
    fun `Skal hente brukernummer for fnr`() {
        val brukernummer = brukernummerDao.hentBrukernummer(Fødselsnummer(("12345678910"))).shouldNotBeNull()
        brukernummer.brukernummer shouldBe "1"
    }
}
