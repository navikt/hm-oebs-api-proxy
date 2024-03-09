package no.nav.hjelpemidler.serviceforespørsel

import no.nav.hjelpemidler.database.Configuration
import no.nav.hjelpemidler.database.list
import no.nav.hjelpemidler.models.SfFeil
import javax.sql.DataSource

class ServiceforespørselFeilDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun finnSfMedFeil(): List<SfFeil> {
        return dataSource.list(
            """
                SELECT ID,
                       SAKSNUMMER,
                       REFERANSENUMMER,
                       PROCESSED,
                       SF_NUMMER,
                       FEILMELDING,
                       KOMMENTAR,
                       CREATION_DATE,
                       LAST_UPDATE_DATE
                FROM apps.xxrtv_cs_digihot_sf_opprett
                WHERE FEILMELDING IS NOT NULL
            """.trimIndent(),
        ) { row ->
            SfFeil(
                id = row.stringOrNull("ID"),
                saksnummer = row.stringOrNull("SAKSNUMMER"),
                referansenummer = row.stringOrNull("REFERANSENUMMER"),
                processed = row.stringOrNull("PROCESSED"),
                sfNummer = row.stringOrNull("SF_NUMMER"),
                feilmelding = row.stringOrNull("FEILMELDING"),
                kommentar = row.stringOrNull("KOMMENTAR"),
                creationDate = row.stringOrNull("CREATION_DATE"),
                lastUpdateDate = row.stringOrNull("LAST_UPDATE_DATE"),
            )
        }
    }
}
