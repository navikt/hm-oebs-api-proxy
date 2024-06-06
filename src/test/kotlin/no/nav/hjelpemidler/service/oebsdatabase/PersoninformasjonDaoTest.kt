package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testDatabase
import kotlin.test.Test

class PersoninformasjonDaoTest {
    @Test
    fun `Skal kunne hente personinformasjon`() = runTest {
        val personinformasjon = testDatabase.transaction {
            personinformasjonDao.hentPersoninformasjon("12345678910")
        }
        personinformasjon shouldHaveSize 2
        personinformasjon.shouldForAll {
            it.brukerNr shouldBe "1"
        }
    }
}
