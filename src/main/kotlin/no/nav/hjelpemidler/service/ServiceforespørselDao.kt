package no.nav.hjelpemidler.service

import kotliquery.Parameter
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.jsonMapper
import no.nav.hjelpemidler.models.Serviceforespørsel
import no.nav.hjelpemidler.models.ServiceforespørselFeil

class ServiceforespørselDao(private val tx: JdbcOperations) {
    fun opprettServiceforespørsel(sf: Serviceforespørsel): Int {
        return tx.update(
            """
                INSERT INTO apps.xxrtv_cs_digihot_sf_opprett
                (id, fnr, navn, stonadsklass, sakstype, resultat, sfdato, referansenummer, kilde, processed, last_update_date,
                 last_updated_by, creation_date, created_by, job_id, saksblokk, beskrivelse, json_artikkelinfo_in)
                VALUES (apps.xxrtv_cs_digihot_sf_opprett_s.nextval, :fnr, :navn, :stonadsklasse, :sakstype, :resultat, SYSDATE,
                        :referansenummer, :kilde, :processed, SYSDATE, :oppdatertAv, SYSDATE, :oppdatertAv, :jobId, 'X', :beskrivelse, :artikler)
            """.trimIndent(),
            mapOf(
                "fnr" to sf.fødselsnummer,
                "navn" to sf.navn,
                "stonadsklasse" to sf.stønadsklasse.name,
                "sakstype" to "S",
                "resultat" to sf.resultat.name,
                "referansenummer" to sf.referansenummer,
                "kilde" to sf.kilde,
                "processed" to "N",
                "oppdatertAv" to no.nav.hjelpemidler.Configuration.OEBS_BRUKER_ID,
                "jobId" to -1,
                "beskrivelse" to Parameter<String?>(sf.problemsammendrag, String::class.java),
                "artikler" to when {
                    sf.artikler.isNullOrEmpty() -> Parameter<String?>(null, String::class.java)
                    else -> jsonMapper.writeValueAsString(sf.artikler)
                },
            ),
        ).actualRowCount
    }

    fun finnFeilendeServiceforespørsler(): List<ServiceforespørselFeil> {
        return tx.list(
            """
                SELECT id,
                       saksnummer,
                       referansenummer,
                       processed,
                       sf_nummer,
                       feilmelding,
                       kommentar,
                       creation_date,
                       last_update_date
                FROM apps.xxrtv_cs_digihot_sf_opprett
                WHERE feilmelding IS NOT NULL
            """.trimIndent(),
        ) { row ->
            ServiceforespørselFeil(
                id = row.stringOrNull("id"),
                saksnummer = row.stringOrNull("saksnummer"),
                referansenummer = row.stringOrNull("referansenummer"),
                processed = row.stringOrNull("processed"),
                sfNummer = row.stringOrNull("sf_nummer"),
                feilmelding = row.stringOrNull("feilmelding"),
                kommentar = row.stringOrNull("kommentar"),
                creationDate = row.stringOrNull("creation_date"),
                lastUpdateDate = row.stringOrNull("last_update_date"),
            )
        }
    }
}
