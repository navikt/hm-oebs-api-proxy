package no.nav.hjelpemidler.service

import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.UpdateResult
import no.nav.hjelpemidler.database.testTransaction
import no.nav.hjelpemidler.models.Resultat
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.SfArtikkel
import no.nav.hjelpemidler.models.Stønadsklasse
import kotlin.test.Test

class ServiceforespørselDaoTest {
    @Test
    fun `Skal opprette serviceforespørsel`() = runTest {
        testTransaction {
            serviceforespørselDao.opprettServiceforespørsel(
                serviceforespørsel(),
            )
        } shouldBe 1
    }

    @Test
    fun `Skal kun sette kontaktopplysninger i dev`() {
        val kontaktOpplysninger = """
            Bestilt av: Formidler Formidlersen, tlf: 11 11 11 11.
            Kontakt ved levering: Annen Annensen, tlf: 22 22 22 22
        """.trimIndent()

        val (devSql, devParams) = fangOpprettelse(isDev = true)
        devSql shouldContain "json_notatinfo_in, kontakt_opplysninger"
        devSql shouldContain ":notat, :kontaktOpplysninger"
        devParams shouldContain ("kontaktOpplysninger" to kontaktOpplysninger)

        val (prodSql, prodParams) = fangOpprettelse(isDev = false)
        prodSql shouldNotContain "kontakt_opplysninger"
        prodSql shouldNotContain ":kontaktOpplysninger"
        prodParams.containsKey("kontaktOpplysninger") shouldBe false
    }

    private fun fangOpprettelse(isDev: Boolean): Pair<String, Map<String, Any?>> {
        val tx = mockk<JdbcOperations>()
        val sql = slot<CharSequence>()
        val params = slot<Map<String, Any?>>()
        every { tx.update(capture(sql), capture(params)) } returns UpdateResult(1)

        ServiceforespørselDao(tx, isDev).opprettServiceforespørsel(serviceforespørsel()) shouldBe 1

        return sql.captured.toString() to params.captured
    }

    private fun serviceforespørsel() = Serviceforespørsel(
        fødselsnummer = "12345678910",
        navn = "",
        stønadsklasse = Stønadsklasse.HJDAAN,
        resultat = Resultat.IM,
        referansenummer = "1",
        problemsammendrag = "1; terskeleliminator",
        artikler = listOf(
            SfArtikkel(hmsnr = "1", antall = "1"),
            SfArtikkel(hmsnr = "2", antall = "1"),
        ),
    )
}
