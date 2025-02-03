package no.nav.hjelpemidler.models

data class Serviceforespørsel(
    val fødselsnummer: String,
    val navn: String,
    val stønadsklasse: Stønadsklasse,
    val resultat: Resultat,
    val referansenummer: String,
    val kilde: String = "DIGIHOT",
    val jobId: String = "-1",
    val problemsammendrag: String? = null,
    val artikler: List<Artikkel>? = null,
) {
    data class Artikkel(override val hmsnr: String, override val antall: String, val shipping_instructions: String = "Tester notat til 5.17") : no.nav.hjelpemidler.models.Artikkel
}

enum class Stønadsklasse(val stønadsklasse: String) {
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
    HJDAAK("Hjelpemidler dagligliv aktivitet"),
}

enum class Resultat(val resultat: String) {
    I("Innvilget"),
    DI("Delvis innvilget"),
    IM("Innvilget muntlig"),
    IN("Innvilget ny situasjon"),
}
