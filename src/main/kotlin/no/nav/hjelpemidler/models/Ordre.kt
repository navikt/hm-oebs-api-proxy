package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class Ordre(
    val fodselsnummer: String,
    val formidlernavn: String,
    val ordretype: OrdeType = OrdeType.BESTILLING,
    val saksnummer: String,
    val artikler: List<Artikkel>
)

data class Artikkel(val hmsnr: String, val antall: String)

enum class OrdeType {
    BESTILLING
}

data class OebsJsonFormat(
    @JsonProperty("P_JSON_MELDING")
    val jsonMelding: String,
    @JsonProperty("P_RETUR_MELDING")
    val returMelding: String = ""
)

fun Ordre.tilOebsJsonFormat(): OebsJsonFormat {
    val writeValueAsString = jacksonObjectMapper().writeValueAsString(this)
    writeValueAsString.replace("\"", "\\\"")
    return OebsJsonFormat(writeValueAsString)
}
