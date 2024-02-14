package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.models.HjelpemiddelBruker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val byttebareIsokoderForBrukerpass = listOf("123903", "TODO kjørehansker")

fun erGyldigIsokodeForBrukerpassbytte(iso: String) = iso.take(6) in byttebareIsokoderForBrukerpass

fun erPermanentUtlån(utlånsType: String?) = utlånsType == UtlånsType.PERMANENT.kode

fun erGyldigTidsbestemtUtlån(item: HjelpemiddelBruker): Boolean {
    val innleveringsdato = (item.oppdatertInnleveringsdato ?: item.innleveringsdato)?.toInnleveringsdato()
        ?: return false
    return item.utlånsType == UtlånsType.TIDSBESTEMT_UTLÅN.kode && LocalDate.now() < innleveringsdato
}

private val oebsDatoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private fun String.toInnleveringsdato() = LocalDateTime.parse(this, oebsDatoFormatter).toLocalDate()

enum class UtlånsType(val kode: String) {
    PERMANENT("P"),
    TIDSBESTEMT_UTLÅN("F"),
    KORTTIDSUTLÅN("K"),
    UTPRØVINGSLÅN("U")
}
