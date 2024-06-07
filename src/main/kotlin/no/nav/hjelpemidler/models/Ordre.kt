package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.hjelpemidler.jsonMapper

data class Ordre(
    val fodselsnummer: String,
    val formidlernavn: String,
    val ordretype: OrdreType = OrdreType.BESTILLING,
    val saksnummer: String,
    val artikler: List<Artikkel>,
    val shippinginstructions: String,
) {
    data class Artikkel(override val hmsnr: String, override val antall: String) : no.nav.hjelpemidler.models.Artikkel
}

enum class OrdreType {
    BESTILLING,
}

data class OebsJsonFormat(
    @JsonProperty("P_JSON_MELDING")
    val jsonMelding: String,
    @JsonProperty("P_RETUR_MELDING")
    val returMelding: String = "",
) {
    constructor(jsonMelding: Any, returMelding: String = "") : this(
        jsonMapper.writeValueAsString(jsonMelding),
        returMelding,
    )
}
