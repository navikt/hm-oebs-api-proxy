package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.models.HjelpemiddelBruker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun berikBytteinfo(item: HjelpemiddelBruker) {
    item.kanByttes = erPermanentUtlån(item.utlånsType) ||
        erGyldigTidsbestemtUtlån(
            item.oppdatertInnleveringsdato,
            item.innleveringsdato,
            item.utlånsType,
        )
    item.kanByttesMedBrukerpass = item.kanByttes!! && erGyldigIsokodeForBrukerpassbytte(item.kategoriNummer)
}

private val byttebareIsokoderForBrukerpass: List<String> = listOf(
    "123903", // Mobilitetsstokk
    "090312", // Hansker og votter (kjørehansker)
)

private fun erGyldigIsokodeForBrukerpassbytte(iso: String): Boolean = iso.take(6) in byttebareIsokoderForBrukerpass

fun erPermanentUtlån(utlånsType: String?): Boolean = utlånsType == Utlånstype.PERMANENT.kode

fun erGyldigTidsbestemtUtlån(
    oppdatertInnleveringsdato: String?,
    innleveringsdato: String?,
    utlånstype: String?,
): Boolean {
    val innleveringsdato = (oppdatertInnleveringsdato ?: innleveringsdato)?.toInnleveringsdato() ?: return false
    return utlånstype == Utlånstype.TIDSBESTEMT_UTLÅN.kode && LocalDate.now() < innleveringsdato
}

private val oebsDatoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private fun String.toInnleveringsdato(): LocalDate = LocalDateTime.parse(this, oebsDatoFormatter).toLocalDate()

enum class Utlånstype(val kode: String) {
    PERMANENT("P"),
    TIDSBESTEMT_UTLÅN("F"),
    KORTTIDSUTLÅN("K"),
    UTPRØVINGSLÅN("U"),
}
