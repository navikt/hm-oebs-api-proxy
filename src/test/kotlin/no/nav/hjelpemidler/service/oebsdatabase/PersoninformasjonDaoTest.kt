package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.database.OracleTestHelper
import kotlin.test.Test

class PersoninformasjonDaoTest {
    private val personinformasjonDao = PersoninformasjonDao(OracleTestHelper.dataSource)

    @Test
    fun `Skal kunne hente personinformasjon`() {
        val personinformasjon = personinformasjonDao.hentPersoninformasjon("12345678910")
        personinformasjon shouldHaveSize 2
        personinformasjon.shouldForAll {
            it.brukerNr shouldBe "1"
        }
    }
}
