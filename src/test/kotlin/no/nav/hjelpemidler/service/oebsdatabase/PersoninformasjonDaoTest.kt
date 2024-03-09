package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import no.nav.hjelpemidler.database.OracleTestHelper
import kotlin.test.Test

class PersoninformasjonDaoTest {
    private val personinformasjonDao = PersoninformasjonDao(OracleTestHelper.dataSource)

    @Test
    fun `Skal kunne hente personinformasjon`() {
        val personinformasjon = personinformasjonDao.hentPersoninformasjon("12345678910")
        personinformasjon.shouldBeSingleton {
            it.brukerNr shouldBe "1"
        }
    }
}
