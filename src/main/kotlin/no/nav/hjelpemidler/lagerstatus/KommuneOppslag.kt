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
        logger.info("read ${kommuneLagerTabell.size} lagre")
    }

    fun hentOrgNavn(kommunenummer: String): String? {
        val lagerKode = kommuneLagerTabell[kommunenummer]
        logger.info("Fant lagerkode $lagerKode fra kommunenummer $kommunenummer")
        val orgNavn = lagerMap[lagerKode]
        logger.info("Fant orgNavn $orgNavn fra lagerkode $lagerKode (kommunenummer=$kommunenummer)")
        return orgNavn
    }
}

val lagerMap: Map<String, String> = mapOf(
    "01" to "*01 Østfold",
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
    "20" to "*20 Finnmark"
)
