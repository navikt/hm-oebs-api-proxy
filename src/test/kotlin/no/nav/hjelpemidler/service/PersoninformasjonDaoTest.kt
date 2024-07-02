package no.nav.hjelpemidler.service

import io.kotest.matchers.collections.shouldBeSingleton
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

        personinformasjon.shouldBeSingleton {
            it.brukerNr shouldBe "1"
            it.aktiv shouldBe true
        }
    }
}
