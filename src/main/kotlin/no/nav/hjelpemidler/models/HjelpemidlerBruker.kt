package no.nav.hjelpemidler.models

data class HjelpemiddelBruker (
    val antall: String,
    val kategori: String,
    val artikkelBeskrivelse: String,
    val artikkelNr: String,
    val serieNr: String?,
    val datoUtsendelse: String,
)

data class HjelpemiddelBrukerOEBS (
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

fun HjelpemiddelBrukerOEBS.toHjelpemiddelBruker(): HjelpemiddelBruker {
    return HjelpemiddelBruker(
        antall = this.ANTALL,
        kategori = this.KATEGORI3_BESKRIVELSE,
        artikkelBeskrivelse = this.ARTIKKEL_BESKRIVELSE,
        artikkelNr = this.ARTIKKELNUMMER,
        serieNr = this.SERIE_NUMMER,
        datoUtsendelse = this.FØRSTE_UTSENDELSE,
    )
}

fun HjelpemiddelBrukerMocks(fnr: String) = listOf(
    HjelpemiddelBrukerOEBS(
        "177946",
        "1234",
        "1",
        "",
        "Rullator til innendørs bruk",
        "I utlån",
        "2000-01-01",
        "",
        "Gemino 20",
        "1000",
        "",
        fnr,
        "Installasjonsveien 1",
        "Installasjonskommunen",
        "1234",
        "Installsjonsbyen",
        "Bostedsveien 2",
        "Bostedskommunen",
        "4321",
        "Bostedsbyen",
    ).toHjelpemiddelBruker(),
    HjelpemiddelBrukerOEBS(
        "021922",
        "2345",
        "2",
        "",
        "Rullator til innendørs bruk",
        "I utlån",
        "2001-02-02",
        "771044",
        "Topro Troja Classic M",
        "1001",
        "",
        fnr,
        "Installasjonsveien 1",
        "Installasjonskommunen",
        "1234",
        "Installsjonsbyen",
        "Bostedsveien 2",
        "Bostedskommunen",
        "4321",
        "Bostedsbyen",
    ).toHjelpemiddelBruker(),
    HjelpemiddelBrukerOEBS(
        "014112",
        "3456",
        "5",
        "",
        "Terskeleliminator",
        "I utlån",
        "2002-03-03",
        "",
        "Topro Terskeleliminator",
        "1002",
        "",
        fnr,
        "Installasjonsveien 1",
        "Installasjonskommunen",
        "1234",
        "Installsjonsbyen",
        "Bostedsveien 2",
        "Bostedskommunen",
        "4321",
        "Bostedsbyen",
    ).toHjelpemiddelBruker(),
)