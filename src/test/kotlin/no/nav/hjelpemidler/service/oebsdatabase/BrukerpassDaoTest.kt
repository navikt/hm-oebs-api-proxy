package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.database.OracleTestHelper
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test

class BrukerpassDaoTest {
    private val brukerpassDao = BrukerpassDao(OracleTestHelper.dataSource)

    @Test
    fun `Skal hente brukerpass fra databasen`() {
        val fnr = "12345678910"
        val brukerpass = brukerpassDao.brukerpassForFnr(fnr)
        brukerpass.brukerpass shouldBe true
        brukerpass.startDate shouldBe LocalDate.of(2020, Month.JANUARY, 1)
    }

    @Test
    fun `Skal svare med brukerpass = false hvis ingen treff i databasen`() {
        val fnr = "10987654321"
        val brukerpass = brukerpassDao.brukerpassForFnr(fnr)
        brukerpass.brukerpass shouldBe false
    }
}
