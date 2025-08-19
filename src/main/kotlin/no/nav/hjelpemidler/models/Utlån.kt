package no.nav.hjelpemidler.models

import java.time.LocalDate

data class Utlån(
    val fnr: String,
    val artnr: String,
    val serienr: String,
    val utlånsDato: String,
    val garantidato: LocalDate? = LocalDate.now().minusMonths(6),
)
