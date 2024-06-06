package no.nav.hjelpemidler.service

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testTransaction
import kotlin.test.Test

class PersoninformasjonDaoTest {
    @Test
    fun `Skal kunne hente personinformasjon`() = runTest {
        val personinformasjon = testTransaction {
            personinformasjonDao.hentPersoninformasjon("12345678910")
        }
        personinformasjon shouldHaveSize 2
        personinformasjon.shouldForAll {
            it.brukerNr shouldBe "1"
        }
    }
}
