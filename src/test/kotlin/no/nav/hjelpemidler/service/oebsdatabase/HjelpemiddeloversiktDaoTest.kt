package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.models.HjelpemiddelBruker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HjelpemiddeloversiktDaoTest {

    @Test
    fun `skal sette gyldig bytte for permanent utlån`() {
        val item = item(type = UtlånsType.PERMANENT)
        berikBytteinfo(item)
        assertTrue(item.kanByttes!!)
    }

    @Test
    fun `skal sette kanByttes false for korttidsutlån`() {
        val item = item(
            type = UtlånsType.KORTTIDSUTLÅN,
            innlevering = LocalDate.now().plusDays(1)
        )
        berikBytteinfo(item)
        assertFalse(item.kanByttes!!)
    }

    @Test
    fun `skal sette kanByttes false ved tidsbestemt utlån som er utgått`() {
        val item = item(innlevering = LocalDate.now().minusDays(1), type = UtlånsType.TIDSBESTEMT_UTLÅN)
        berikBytteinfo(item)
        assertFalse(item.kanByttes!!)
    }

    @Test
    fun `skal bruke oppdaterInnleveringsdato for sjekk av gyldig bytte, dersom den er satt`() {
        val item = item(
            innlevering = LocalDate.now().minusDays(1),
            oppdatertInnlevering = LocalDate.now().plusDays(1),
            type = UtlånsType.TIDSBESTEMT_UTLÅN
        )
        berikBytteinfo(item)
        assertTrue(item.kanByttes!!)
    }
}

private fun item(
    type: UtlånsType = UtlånsType.PERMANENT,
    innlevering: LocalDate? = LocalDate.now(),
    oppdatertInnlevering: LocalDate? = null
) = HjelpemiddelBruker(
    antall = "",
    antallEnhet = "",
    kategoriNummer = "",
    kategori = "",
    artikkelBeskrivelse = "",
    artikkelNr = "",
    serieNr = "",
    datoUtsendelse = "",
    ordrenummer = "",
    artikkelStatus = "",
    utlånsType = type.kode,
    innleveringsdato = innlevering?.let(::toInnleveringsdato),
    oppdatertInnleveringsdato = oppdatertInnlevering?.let(::toInnleveringsdato)
)

private val oebsDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
fun toInnleveringsdato(dato: LocalDate = LocalDate.now().plusYears(1)): String =
    LocalDateTime.of(dato, LocalTime.of(0, 0)).format(oebsDateTimeFormat)
