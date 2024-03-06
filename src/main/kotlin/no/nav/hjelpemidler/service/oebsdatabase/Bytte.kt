package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.models.HjelpemiddelBruker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun berikBytteinfo(item: HjelpemiddelBruker) {
    item.kanByttes = erPermanentUtlån(item.utlånsType) || erGyldigTidsbestemtUtlån(
        item.oppdatertInnleveringsdato,
        item.innleveringsdato,
        item.utlånsType,
    )
    item.kanByttesMedBrukerpass = item.kanByttes!! && erGyldigIsokodeForBrukerpassbytte(item.kategoriNummer)
}

private val byttebareIsokoderForBrukerpass = listOf(
    "123903", // Mobilitetsstokk
    "090312", // Hansker og votter (kjørehansker)
)

private fun erGyldigIsokodeForBrukerpassbytte(iso: String) = iso.take(6) in byttebareIsokoderForBrukerpass

fun erPermanentUtlån(utlånsType: String?) = utlånsType == UtlånsType.PERMANENT.kode

fun erGyldigTidsbestemtUtlån(
    oppdatertInnleveringsdato: String?,
    innleveringsdato: String?,
    utlånsType: String?
): Boolean {
    val innleveringsdato = (oppdatertInnleveringsdato ?: innleveringsdato)?.toInnleveringsdato()
        ?: return false
    return utlånsType == UtlånsType.TIDSBESTEMT_UTLÅN.kode && LocalDate.now() < innleveringsdato
}

private val oebsDatoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private fun String.toInnleveringsdato() = LocalDateTime.parse(this, oebsDatoFormatter).toLocalDate()

enum class UtlånsType(val kode: String) {
    PERMANENT("P"),
    TIDSBESTEMT_UTLÅN("F"),
    KORTTIDSUTLÅN("K"),
    UTPRØVINGSLÅN("U"),
}
