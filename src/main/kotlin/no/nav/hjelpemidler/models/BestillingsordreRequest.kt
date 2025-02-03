package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class BestillingsordreRequest(
    val fodselsnummer: String,
    val formidlernavn: String,
    val saksnummer: String,
    val artikler: List<Artikkel>,
    val forsendelsesinfo: String? = null,
    val ferdigstillOrdre: Boolean? = true,
) {
    /**
     * NB! Vi fjerner evt. whitespace og punktum i [forsendelsesinfo] da det kan gi feil i prosessering i OeBS.
     */
    val shippinginstructions
        @JsonIgnore get() = when {
            forsendelsesinfo.isNullOrBlank() -> formidlernavn
            else -> forsendelsesinfo.trim().removeSuffix(".")
        }

    data class Artikkel(
        override val hmsnr: String,
        override val antall: String,
    ) : no.nav.hjelpemidler.models.Artikkel
}
