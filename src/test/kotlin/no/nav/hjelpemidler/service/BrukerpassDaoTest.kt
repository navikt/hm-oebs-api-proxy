package no.nav.hjelpemidler.service

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testTransaction
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.år
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test

class BrukerpassDaoTest {
    @Test
    fun `Skal hente brukerpass fra databasen`() = runTest {
        val fnr = "19127428657"
        val brukerpass = testTransaction {
            brukerpassDao.brukerpassForFnr(fnr)
        }
        brukerpass.brukerpass shouldBe true
        brukerpass.startDate shouldBe LocalDate.of(2020, Month.JANUARY, 1)
    }

    @Test
    fun `Skal svare med brukerpass = false hvis ingen treff i databasen`() = runTest {
        val fnr = Fødselsnummer(60.år)
        val brukerpass = testTransaction {
            brukerpassDao.brukerpassForFnr(fnr.value)
        }
        brukerpass.brukerpass shouldBe false
    }
}
