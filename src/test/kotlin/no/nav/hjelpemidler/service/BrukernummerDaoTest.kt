package no.nav.hjelpemidler.service

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testDatabase
import no.nav.hjelpemidler.models.Fødselsnummer
import kotlin.test.Test

class BrukernummerDaoTest {
    @Test
    fun `Skal hente brukernummer for fnr`() = runTest {
        val brukernummer = testDatabase.transaction {
            brukernummerDao.hentBrukernummer(Fødselsnummer(("12345678910"))).shouldNotBeNull()
        }
        brukernummer.brukernummer shouldBe "1"
    }
}
