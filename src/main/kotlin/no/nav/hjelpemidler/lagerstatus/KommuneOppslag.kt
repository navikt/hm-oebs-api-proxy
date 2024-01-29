package no.nav.hjelpemidler.lagerstatus

import mu.KotlinLogging
import java.io.IOException

private const val FILENAME = "lager/regionstabell.csv"
private val logger = KotlinLogging.logger { }

class KommuneOppslag {
    private val kommuneLagerTabell: MutableMap<String, String> = mutableMapOf()

    init {
        val csvSplitBy = ";"
        javaClass.classLoader.getResourceAsStream(FILENAME)?.bufferedReader()
            ?.forEachLine { line ->
                val splitLine = line.split(csvSplitBy).toTypedArray()
                val lager: String = splitLine[0]
                val kommunenummer: String = splitLine[1]

                if (lager.isNotBlank() && kommunenummer.isNotBlank()) {
                    kommuneLagerTabell[kommunenummer] = lager
                } else {
                    throw IOException("There was an error parsing data from file for kommunenummer '$kommunenummer' and lagernummer `$lager`")
                }
            }
        logger.info("Leste ${kommuneLagerTabell.size} lagre fra regionstabell.")
    }

    fun hentOrgNavn(kommunenummer: String): String? {
        val lagerKode = kommuneLagerTabell[kommunenummer]
        val orgNavn = lagerMap[lagerKode]
        logger.info("Fant orgNavn $orgNavn fra lagerkode $lagerKode. Kommunenummer = $kommunenummer")
        return orgNavn
    }
}

val lagerMap: Map<String, String> = mapOf(
    "03" to "*03 Oslo",
    "11" to "*11 Rogaland",
    "15" to "*15 Møre og Romsdal",
    "18" to "*18 Nordland",
    "31" to "*31 Østfold",
    "32" to "*32 Akershus",
    "33" to "*33 Buskerud",
    "34" to "*34 Innlandet",
    "39" to "*39 Vestfold",
    "40" to "*40 Telemark",
    "42" to "*42 Agder",
    "46" to "*46 Vestland",
    "50" to "*50 Trøndelag",
    "55" to "*55 Troms",
    "56" to "*56 Finnmark",
    "21" to "*21 Svalbard",
    "22" to "*22 Jan Mayen",
)
