package no.nav.hjelpemidler.models

data class Utlån(
    val fnr: String,
    val artnr: String,
    val serienr: String,
    val utlånsDato: String,
    val opprettetDato: String, // opprettet dato er det samme som garantidato i OeBS
    // val garantidato: LocalDate? = LocalDate.now().minusMonths(6),
    val isokode: String,
)
