package no.nav.hjelpemidler

import no.nav.hjelpemidler.OracleTestHelper.withDb
import no.nav.hjelpemidler.service.oebsdatabase.PersoninformasjonDao
import org.junit.Ignore
import org.junit.Test

internal class PersoninformasjonDaoTest {

    @Test
    @Ignore
    fun `skal kunne hente personinformasjon`() {
        withDb {
            with(PersoninformasjonDao(OracleTestHelper.dataSource)) {
                hentPersoninformasjon("123")
            }
        }
    }
}
