package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.database.Configuration
import no.nav.hjelpemidler.database.singleOrNull
import no.nav.hjelpemidler.models.Fødselsnummer
import javax.sql.DataSource

class BrukernummerDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun hentBrukernummer(fnr: Fødselsnummer): Brukernummer? {
        return dataSource.singleOrNull(
            """
                SELECT BRUKER_NUMMER
                FROM apps.XXRTV_DIGIHOT_OEBS_ADR_FNR_V
                WHERE FNR = :fnr
                FETCH NEXT 1 ROW ONLY
            """.trimIndent(),
            mapOf("fnr" to fnr.value),
        ) { row ->
            Brukernummer(
                brukernummer = row.string("BRUKER_NUMMER"),
            )
        }
    }
}

data class Brukernummer(val brukernummer: String)
