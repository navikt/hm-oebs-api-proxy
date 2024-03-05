package no.nav.hjelpemidler.models

data class Serviceforespørsel(
    val fødselsnummer: String,
    val navn: String,
    val stønadsklasse: Stønadsklasse,
    val resultat: Resultat,
    val referansenummer: String,
    val kilde: String = "DIGIHOT",
    val jobId: String = "-1",
    val problemsammendrag: String?,
)

enum class Stønadsklasse(stønadsklasse: String) {
    HJARAN("Hjelpemidler arbeidsliv annet"),
    HJARAU("Hjelpemidler arbeidsliv arbeidstaker utlån"),
    HJARSU("Hjelpemidler arbeidsliv selvstendig næringsdrivende' utlån"),
    HJDAAN("Hjelpemidler dagligliv annet"),
    HJDABA("Hjelpemidler dagligliv barnehage"),
    HJDABH("Hjelpemidler dagligliv behandlingshjelpemidler"),
    HJDALÆ("Hjelpemidler dagligliv lærlinger"),
    HJDASK("Hjelpemidler dagligliv skole"),
    HJDATR("Hjelpemidler dagligliv trening"),
    HJFØ("Hjelpemidler dagligliv førerhunder"),
    HJSH("Hjelpemidler dagligliv servicehunder"),
    HJDAAK("Hjelpemidler dagligliv aktivitet")
}

enum class Resultat(resultat: String) {
    I("Innvilget"),
    DI("Delvis innvilget"),
    IM("Innvilget muntlig"),
    IN("Innvilget ny situasjon")
}
