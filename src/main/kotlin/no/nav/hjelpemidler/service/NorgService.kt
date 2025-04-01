package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.client.NorgClient

class NorgService(private val norgClient: NorgClient) {
    suspend fun hentEnhetNavn(kommunenummer: String): String? {
        val enhetNr = norgClient.hentArbeidsfordelingenheter(kommunenummer).first().enhetNr
        return hentEnhetNavnForEnhetnr(enhetNr)
    }

    fun hentEnhetNavnForEnhetnr(enhetNr: String): String? {
        return lagerMap[enhetNr.takeLast(2)]
    }
}

val lagerMap: Map<String, String> = mapOf(
    "01" to "*01 Østfold",
    "02" to "*03 Oslo",
    "03" to "*03 Oslo",
    "04" to "*04 Hedmark",
    "05" to "*05 Oppland",
    "06" to "*06 Buskerud",
    "07" to "*07 Vestfold",
    "08" to "*08 Telemark",
    "09" to "*09 Aust-Agder",
    "10" to "*10 Vest-Agder",
    "11" to "*11 Rogaland",
    "12" to "*12 Hordaland",
    "14" to "*14 Sogn og Fjordane",
    "15" to "*15 Møre og Romsdal",
    "16" to "*16 Sør-Trøndelag",
    "17" to "*17 Nord-Trøndelag",
    "18" to "*18 Nordland",
    "19" to "*19 Troms",
    "20" to "*20 Finnmark",
)
