package no.nav.hjelpemidler.service.oebsdatabase

import io.kotest.matchers.collections.shouldHaveSize
import no.nav.hjelpemidler.database.OracleTestHelper
import kotlin.test.Test

class TittelForHmsnrDaoTest {
    private val tittelForHmsnrDao = TittelForHmsnrDao(OracleTestHelper.dataSource)

    @Test
    fun `Skal hente titler for artikler`() {
        val result = tittelForHmsnrDao.hentTittelForHmsnrs(setOf("1", "2", "3"))
        result shouldHaveSize 1
    }
}
