package no.nav.hjelpemidler.models

import com.fasterxml.jackson.annotation.JsonAlias

interface Artikkel {
    @get:JsonAlias("hmsArtNr")
    val hmsnr: String
    val antall: String
}
