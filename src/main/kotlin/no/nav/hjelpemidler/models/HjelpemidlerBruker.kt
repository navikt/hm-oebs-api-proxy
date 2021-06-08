package no.nav.hjelpemidler.models

data class HjelpemiddelBruker (
    val antall: String,
    val antallEnhet: String,
    val kategori: String,
    val artikkelBeskrivelse: String,
    val artikkelNr: String,
    val serieNr: String?,
    val datoUtsendelse: String,

    var hmdbBeriket: Boolean,
    var hmdbProduktNavn: String?,
    var hmdbBeskrivelse: String?,
    var hmdbKategori: String?,
    var hmdbBilde: String?,

    /*
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
        val ENHET: String,

        val BRUKER_NUMMER: String,
        val FNR: String,

        val EGEN_ANSATT: String,

        val INSTALLASJONS_ADDRESSE: String,
        val INSTALLASJONS_KOMMUNE: String,
        val INSTALLASJONS_POSTNUMMER: String,
        val INSTALLASJONS_BY: String,

        val BOSTEDS_ADDRESSE: String,
        val BOSTEDS_KOMMUNE: String,
        val BOSTEDS_POSTNUMMER: String,
        val BOSTEDS_BY: String,
     */
)
