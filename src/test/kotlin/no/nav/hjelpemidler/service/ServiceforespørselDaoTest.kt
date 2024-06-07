package no.nav.hjelpemidler.service

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.testTransaction
import no.nav.hjelpemidler.models.Resultat
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.Stønadsklasse
import kotlin.test.Test

class ServiceforespørselDaoTest {
    @Test
    fun `Skal opprette serviceforespørsel`() = runTest {
        testTransaction {
            serviceforespørselDao.opprettServiceforespørsel(
                Serviceforespørsel(
                    fødselsnummer = "12345678910",
                    navn = "",
                    stønadsklasse = Stønadsklasse.HJDAAN,
                    resultat = Resultat.IM,
                    referansenummer = "1",
                    problemsammendrag = "1; terskeleliminator",
                    artikler = listOf(
                        Serviceforespørsel.Artikkel(hmsnr = "1", antall = "1"),
                        Serviceforespørsel.Artikkel(hmsnr = "2", antall = "1"),
                    ),
                ),
            )
        } shouldBe 1
    }
}
