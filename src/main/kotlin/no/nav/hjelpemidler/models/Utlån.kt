package no.nav.hjelpemidler.models

import java.time.LocalDate

data class Utlån(
    val fnr: String,
    val artnr: String,
    val serienr: String,
    val utlånsDato: String,
    val opprettetDato: LocalDate?, // opprettet dato er det samme som garantidato i OeBS
    val isokode: String?,
)
