package no.nav.hjelpemidler.service

import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testDatabase
import kotlin.test.Test

class TittelForHmsnrDaoTest {
    @Test
    fun `Skal hente titler for artikler`() = runTest {
        val result = testDatabase.transaction {
            tittelForHmsnrDao.hentTittelForHmsnrs(setOf("1", "2", "3"))
        }
        result shouldHaveSize 1
    }
}
