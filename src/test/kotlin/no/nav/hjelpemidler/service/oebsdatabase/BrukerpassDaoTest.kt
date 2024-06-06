package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testDatabase
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test

class BrukerpassDaoTest {
    @Test
    fun `Skal hente brukerpass fra databasen`() = runTest {
        val fnr = "12345678910"
        val brukerpass = testDatabase.transaction {
            brukerpassDao.brukerpassForFnr(fnr)
        }
        brukerpass.brukerpass shouldBe true
        brukerpass.startDate shouldBe LocalDate.of(2020, Month.JANUARY, 1)
    }

    @Test
    fun `Skal svare med brukerpass = false hvis ingen treff i databasen`() = runTest {
        val fnr = "10987654321"
        val brukerpass = testDatabase.transaction {
            brukerpassDao.brukerpassForFnr(fnr)
        }
        brukerpass.brukerpass shouldBe false
    }
}
