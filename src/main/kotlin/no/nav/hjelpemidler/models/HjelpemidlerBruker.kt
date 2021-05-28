package no.nav.hjelpemidler.models

data class HjelpemiddelBruker (
    val FOREKOMST_NUMMER: String,
    val ORDRE_NUMMER: String,

    val ANTALL: String,

    val KATEGORI3_NUMMER: String,
    val KATEGORI3_BESKRIVELSE: String,

    val ARTIKKELSTATUS: String,
    val FØRSTE_UTSENDELSE: String,

    val SERIE_NUMMER: String,

    val ARTIKKEL_BESKRIVELSE: String,
    val ARTIKKELNUMMER: String,

    val BRUKER_NUMMER: String,
    val FNR: String,

    val INSTALLASJONS_ADDRESSE: String,
    val INSTALLASJONS_KOMMUNE: String,
    val INSTALLASJONS_POSTNUMMER: String,
    val INSTALLASJONS_BY: String,

    val BOSTEDS_ADDRESSE: String,
    val BOSTEDS_KOMMUNE: String,
    val BOSTEDS_POSTNUMMER: String,
    val BOSTEDS_BY: String,
)
