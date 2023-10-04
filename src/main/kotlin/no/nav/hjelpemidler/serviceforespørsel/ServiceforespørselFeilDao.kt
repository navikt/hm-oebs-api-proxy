package no.nav.hjelpemidler.serviceforespørsel

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.SfFeil
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class ServiceforespørselFeilDao(private val dataSource: DataSource = Configuration.dataSource) {

    fun finnSfMedFeil(): List<SfFeil> {
        @Language("OracleSQL")
        val finnSFQuery =
            """
            select ID, SAKSNUMMER, REFERANSENUMMER, PROCESSED, SF_NUMMER, FEILMELDING, KOMMENTAR, CREATION_DATE, LAST_UPDATE_DATE  from apps.xxrtv_cs_digihot_sf_opprett
            where FEILMELDING IS NOT NULL
            """.trimIndent()

        val items = sessionOf(dataSource).use {
            it.run(
                queryOf(
                    finnSFQuery
                ).map { row ->
                    SfFeil(
                        id = row.stringOrNull("ID"),
                        saksnummer = row.stringOrNull("SAKSNUMMER"),
                        referansenummer = row.stringOrNull("REFERANSENUMMER"),
                        processed = row.stringOrNull("PROCESSED"),
                        sfNummer = row.stringOrNull("SF_NUMMER"),
                        feilmelding = row.stringOrNull("FEILMELDING"),
                        kommentar = row.stringOrNull("KOMMENTAR"),
                        creationDate = row.stringOrNull("CREATION_DATE"),
                        lastUpdateDate = row.stringOrNull("LAST_UPDATE_DATE")
                    )
                }.asList
            )
        }
        return items
    }
}
