package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// commented out unused fields. Removed from backend service to reduce file size
@JsonIgnoreProperties(ignoreUnknown = true)
data class HjelpemiddelProdukt(
        val stockid: String?,
        val artid: String?,
        val prodid: String?,
        val artno: String?,
        val artname: String?,
        val adescshort: String?,
        val prodname: String?,
        val pshortdesc: String?,
//        val aindate: String?,
//        val aoutdate: String?,
//        val ldbid: String?,
//        val aout: String?,
//        val aisapproved: String?,
//        val anbudid: String?,
//        val hasanbud: String?,
        val adraft: String?,
        val artpostid: String?,
        val apostid: String?,
        val postrank: String?,
        val apostnr: String?,
        val aposttitle: String?,
//        val apostdesc: String?,
        val newsid: String?,
        val blobfileURL: String?,
//        val blobfileURL_snet: String?,
//        val blobtype: String?,
//        val blobuse: String?,
//        val supplier: String?,
//        val pisapproved: String?,
        val isocode: String?,
        val isotitle: String?,
//        val isotextshort: String?,
//        val isactive: String?,
        var kategori: String?,
        var techdataAsText: String?,
        val cleanTechdataAsText: String?,
        val cleanposttitle: String?
)

/*
"        \"stockid\": \"209592\",\n" +
"        \"artid\": 90233,\n" +
"        \"prodid\": 45805,\n" +
"        \"artno\": \"NEA4740\",\n" +
"        \"artname\": \"Nima Equagel Allround\",\n" +
"        \"adescshort\": \"\",\n" +
"        \"prodname\": \"Nima Equagel Allround\",\n" +
"        \"pshortdesc\": \"Trykkavlastende sittepute i to-lags gelmateriale. Puten skal ikke innstilles. Passer til rullestoler med plan- og hengekøyesete. Leveres med to trekk. Høyde: 7,5. Leveres i bredder fra 27 til 50 cm.\",\n" +
"        \"aindate\": \"2015-06-26T15:21:22.953Z\",\n" +
"        \"aoutdate\": null,\n" +
"        \"ldbid\": 2,\n" +
"        \"aout\": false,\n" +
"        \"aisapproved\": true,\n" +
"        \"anbudid\": null,\n" +
"        \"hasanbud\": false,\n" +
"        \"adraft\": null,\n" +
"        \"artpostid\": 16537,\n" +
"        \"apostid\": 866,\n" +
"        \"postrank\": 4,\n" +
"        \"apostnr\": 3,\n" +
"        \"aposttitle\": \"Post 3: Sittepute som ikke skal innstilles - høy modell \",\n" +
"        \"apostdesc\": \"Sitteputer til voksne og barn som har nedsatt evne til å endre sittestilling og som har behov for mye materiale å synke ned i/bli omsluttet av. Brukerne har ikke feilstillinger og/eller asymmetrier.  \",\n" +
"        \"newsid\": 4361,\n" +
"        \"blobfileURL\": \"https://www.hjelpemiddeldatabasen.no/blobs/snet/45805.jpg\",\n" +
"        \"blobfileURL_snet\": \"https://www.hjelpemiddeldatabasen.no/blobs/orig/45805.jpg\",\n" +
"        \"blobtype\": \"billede        \",\n" +
"        \"blobuse\": \"1    \",\n" +
"        \"supplier\": 55790,\n" +
"        \"pisapproved\": true,\n" +
"        \"isocode\": \"18100601\",\n" +
"        \"isotitle\": \"Sitteputer for komfort\",\n" +
"        \"isotextshort\": \"Sitteputer som gir komfort og som ikke er tilbehør til spesielle produkter. Omfatter f.eks. puter til bruk i rullestoler. \",\n" +
"        \"isactive\": true\n" +*/

@JsonIgnoreProperties(ignoreUnknown = true)
data class HjelpemiddelProduktTechDataAttribute(
        val prodid: String,
        val artid: String,
        val techlabeldk: String?,
        val datavalue: String?,
        val techdataunit: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class HjelpemiddelProduktTechDataAttributesInWords(
        val prodid: String?,
        val artid: String?,
        val techdata: String?
)

/*{
    "prodid": 30418,
    "artid": 1507,
    "techlabeldk": "Påkjøring forfra",
    "datavalue": "JA",
    "techdataunit": ""
}*/
