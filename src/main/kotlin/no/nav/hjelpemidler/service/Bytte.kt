package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.models.KanIkkeByttesGrunn
import no.nav.hjelpemidler.models.UtlånMedProduktinfo
import no.nav.hjelpemidler.models.Utlånstype
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun UtlånMedProduktinfo.berikBytteinfo() {
    kanIkkeByttesGrunner = buildList {
        if (!erPermanentUtlån() && !erGyldigTidsbestemtUtlån()) {
            add(KanIkkeByttesGrunn.IKKE_RIKTIG_UTLÅNSTYPE)
        }

        if (erFørstegangsutlevertMadrass()) {
            add(KanIkkeByttesGrunn.ER_FØRSTEGANGSUTLEVERT_MADRASS)
        }
    }
    kanByttes = kanIkkeByttesGrunner?.isEmpty()
    kanByttesMedBrukerpass = kanByttes!! && harGyldigIsokodeForBrukerpassbytte()
}

fun UtlånMedProduktinfo.erPermanentUtlån(): Boolean = utlånsType == Utlånstype.PERMANENT.kode

fun UtlånMedProduktinfo.erGyldigTidsbestemtUtlån(): Boolean {
    val innleveringsdato = (oppdatertInnleveringsdato ?: innleveringsdato)?.toInnleveringsdato() ?: return false
    return utlånsType == Utlånstype.TIDSBESTEMT_UTLÅN.kode && LocalDate.now() < innleveringsdato
}

fun UtlånMedProduktinfo.harGyldigIsokodeForBrukerpassbytte(): Boolean = kategoriNummer.take(6) in byttebareIsokoderForBrukerpass

// Med "førstegangsutlevert madrass" menes madrasser som leveres sammen med sykesenger, så bruker raskt kan ta i bruk sengen.
// Disse madrassene er forbruk og kan ikke byttes inn, og skal heller ikke leveres tilbake til NAV.
// Disse identifiseres med at de har isokode 181218 og har "puls" eller "hypnos" et sted i navnet sitt.
private fun UtlånMedProduktinfo.erFørstegangsutlevertMadrass(): Boolean {
    val isokodeMadrass = "181218"
    val navnPåFørstegangsmadrasser = setOf("puls", "hypnos")
    val hjmNavn = hmdbProduktNavn?.lowercase() ?: return false

    return kategoriNummer.take(6) == isokodeMadrass &&
        navnPåFørstegangsmadrasser.any { hjmNavn.contains(it) }
}

private val oebsDatoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private fun String.toInnleveringsdato(): LocalDate = LocalDateTime.parse(this, oebsDatoFormatter).toLocalDate()

private val byttebareIsokoderForBrukerpass: List<String> = listOf(
    "123903", // Mobilitetsstokk
    "090312", // Hansker og votter (kjørehansker)
)
