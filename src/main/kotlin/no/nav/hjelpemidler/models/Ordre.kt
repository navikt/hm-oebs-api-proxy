package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.hjelpemidler.serialization.jackson.jsonMapper

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Ordre(
    val fodselsnummer: String,
    val formidlernavn: String,
    val ordretype: OrdreType = OrdreType.BESTILLING,
    val saksnummer: String,
    val artikler: List<Artikkel>,
    val shippinginstructions: String,
    val ferdigstill: String? = null,
) {
    data class Artikkel(override val hmsnr: String, override val antall: String) : no.nav.hjelpemidler.models.Artikkel {
        constructor(artikkel: BestillingsordreRequest.Artikkel) : this(artikkel.hmsnr, artikkel.antall)
    }
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
