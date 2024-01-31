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
                val kommunenummer: String = splitLine[0]
                val lager: String = splitLine[1]

                if (lager.isNotBlank() && kommunenummer.isNotBlank()) {
                    kommuneLagerTabell[kommunenummer] = lager
                } else {
                    throw IOException("There was an error parsing data from file for kommunenummer '$kommunenummer' and lagernummer `$lager`")
                }
            }
        logger.info("Leste ${kommuneLagerTabell.size} lagre fra regionstabell.")
    }

    fun hentOrgNavn(kommunenummer: String): String? {
        val orgNavn = kommuneLagerTabell[kommunenummer]
        logger.info("Fant orgNavn $orgNavn for kommunenummer $kommunenummer")
        return orgNavn
    }
}