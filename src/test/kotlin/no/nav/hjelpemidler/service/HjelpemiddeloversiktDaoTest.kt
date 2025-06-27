package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.models.KanIkkeByttesGrunn
import no.nav.hjelpemidler.models.UtlånMedProduktinfo
import no.nav.hjelpemidler.models.Utlånstype
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.Test

class HjelpemiddeloversiktDaoTest {
    @Test
    fun `Skal sette gyldig bytte for permanent utlån`() {
        val utlån = utlån(type = Utlånstype.PERMANENT)
        utlån.berikBytteinfo()
        assertTrue(utlån.kanByttes!!)
    }

    @Test
    fun `Skal sette kanByttes false for korttidsutlån`() {
        val utlån = utlån(
            type = Utlånstype.KORTTIDSUTLÅN,
            innlevering = LocalDate.now().plusDays(1),
        )
        utlån.berikBytteinfo()
        assertFalse(utlån.kanByttes!!)
    }

    @Test
    fun `Skal sette kanByttes false ved tidsbestemt utlån som er utgått`() {
        val utlån = utlån(innlevering = LocalDate.now().minusDays(1), type = Utlånstype.TIDSBESTEMT_UTLÅN)
        utlån.berikBytteinfo()
        assertFalse(utlån.kanByttes!!)
        assertTrue(utlån.kanIkkeByttesGrunner!!.contains(KanIkkeByttesGrunn.IKKE_RIKTIG_UTLÅNSTYPE))
    }

    @Test
    fun `Skal bruke oppdaterInnleveringsdato for sjekk av gyldig bytte, dersom den er satt`() {
        val utlån = utlån(
            innlevering = LocalDate.now().minusDays(1),
            oppdatertInnlevering = LocalDate.now().plusDays(1),
            type = Utlånstype.TIDSBESTEMT_UTLÅN,
        )
        utlån.berikBytteinfo()
        assertTrue(utlån.kanByttes!!)
    }

    @Test
    fun `Skal være mulig for brukerpass å bytte hjelpemidler innenfor godtatt isokode`() {
        val utlån = utlån(
            type = Utlånstype.PERMANENT,
            kategoriNummer = "123903",

        )
        utlån.berikBytteinfo()
        assertTrue(utlån.kanByttes!!)
        assertTrue(utlån.kanByttesMedBrukerpass!!)
    }

    @Test
    fun `Skal ikke være mulig for brukerpass å bytte hjelpemidler utenfor godtatt isokode`() {
        val utlån = utlån(
            type = Utlånstype.PERMANENT,
            kategoriNummer = "111111",

        )
        utlån.berikBytteinfo()
        assertTrue(utlån.kanByttes!!)
        assertFalse(utlån.kanByttesMedBrukerpass!!)
    }

    @Test
    fun `Skal ikke kunne bytte førstegangsutlevert madrass`() {
        val utlån = utlån(
            kategoriNummer = "18121801",
            navn = "Madrass Hypnos I TDI",
        )
        utlån.berikBytteinfo()
        assertFalse(utlån.kanByttes!!)
        assertTrue(utlån.kanIkkeByttesGrunner!!.contains(KanIkkeByttesGrunn.ER_FØRSTEGANGSUTLEVERT_MADRASS))
    }
}

private fun utlån(
    type: Utlånstype = Utlånstype.PERMANENT,
    innlevering: LocalDate? = LocalDate.now(),
    oppdatertInnlevering: LocalDate? = null,
    kategoriNummer: String = "",
    navn: String? = null,
) = UtlånMedProduktinfo(
    antall = "",
    antallEnhet = "",
    kategoriNummer = kategoriNummer,
    kategori = "",
    artikkelBeskrivelse = "",
    artikkelNr = "",
    serieNr = "",
    datoUtsendelse = "",
    ordrenummer = "",
    artikkelStatus = "",
    utlånsType = type.kode,
    innleveringsdato = innlevering?.let(::toInnleveringsdato),
    oppdatertInnleveringsdato = oppdatertInnlevering?.let(::toInnleveringsdato),
    hmdbProduktNavn = navn,
)

private val oebsDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
fun toInnleveringsdato(dato: LocalDate = LocalDate.now().plusYears(1)): String = LocalDateTime.of(dato, LocalTime.of(0, 0)).format(oebsDateTimeFormat)
