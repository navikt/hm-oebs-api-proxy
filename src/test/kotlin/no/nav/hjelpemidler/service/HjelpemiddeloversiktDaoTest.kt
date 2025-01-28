package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.models.HjelpemiddelBruker
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
        val item = item(type = Utlånstype.PERMANENT)
        berikBytteinfo(item)
        assertTrue(item.kanByttes!!)
    }

    @Test
    fun `Skal sette kanByttes false for korttidsutlån`() {
        val item = item(
            type = Utlånstype.KORTTIDSUTLÅN,
            innlevering = LocalDate.now().plusDays(1),
        )
        berikBytteinfo(item)
        assertFalse(item.kanByttes!!)
    }

    @Test
    fun `Skal sette kanByttes false ved tidsbestemt utlån som er utgått`() {
        val item = item(innlevering = LocalDate.now().minusDays(1), type = Utlånstype.TIDSBESTEMT_UTLÅN)
        berikBytteinfo(item)
        assertFalse(item.kanByttes!!)
    }

    @Test
    fun `Skal bruke oppdaterInnleveringsdato for sjekk av gyldig bytte, dersom den er satt`() {
        val item = item(
            innlevering = LocalDate.now().minusDays(1),
            oppdatertInnlevering = LocalDate.now().plusDays(1),
            type = Utlånstype.TIDSBESTEMT_UTLÅN,
        )
        berikBytteinfo(item)
        assertTrue(item.kanByttes!!)
    }

    @Test
    fun `Skal være mulig for brukerpass å bytte hjelpemidler innenfor godtatt isokode`() {
        val item = item(
            type = Utlånstype.PERMANENT,
            kategoriNummer = "123903",

        )
        berikBytteinfo(item)
        assertTrue(item.kanByttes!!)
        assertTrue(item.kanByttesMedBrukerpass!!)
    }

    @Test
    fun `Skal ikke være mulig for brukerpass å bytte hjelpemidler utenfor godtatt isokode`() {
        val item = item(
            type = Utlånstype.PERMANENT,
            kategoriNummer = "111111",

        )
        berikBytteinfo(item)
        assertTrue(item.kanByttes!!)
        assertFalse(item.kanByttesMedBrukerpass!!)
    }
}

private fun item(
    type: Utlånstype = Utlånstype.PERMANENT,
    innlevering: LocalDate? = LocalDate.now(),
    oppdatertInnlevering: LocalDate? = null,
    kategoriNummer: String = "",
) = HjelpemiddelBruker(
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
)

private val oebsDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
fun toInnleveringsdato(dato: LocalDate = LocalDate.now().plusYears(1)): String = LocalDateTime.of(dato, LocalTime.of(0, 0)).format(oebsDateTimeFormat)
