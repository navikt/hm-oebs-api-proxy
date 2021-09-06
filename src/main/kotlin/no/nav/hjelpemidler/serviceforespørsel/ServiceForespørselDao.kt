package no.nav.hjelpemidler.serviceforespørsel

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.models.Serviceforespørsel
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class ServiceforespørselDao (private val dataSource: DataSource = Configuration.dataSource) {

    fun opprettServiceforespørsel(sf: Serviceforespørsel){

        @Language("OracleSQL")
        val opprettSFQuery = """
            
            insert into xxrtv_cs_digihot_sf_opprett 
            (ID, FNR, NAVN, STONADSKLASS,SAKSTYPE,RESULTAT,SFDATO ,REFERANSENUMMER,KILDE,PROCESSED,LAST_UPDATE_DATE,LAST_UPDATED_BY,CREATION_DATE,CREATED_BY, JOB_ID)
            values 
            (XXRTV_CS_DIGIHOT_SF_OPPRETT_S.nextval, :fnr, :navn, :stonadsklasse, :sakstype, :resultat, sysdate, :referansenummer, :kilde, :processed, sysdate, :oppdatertAv, sysdate, :oppdatertAv, :jobId)
        """.trimIndent()

        sessionOf(dataSource).use {
            it.run(
                queryOf(opprettSFQuery, mapOf(
                "fnr" to sf.fødselsnummer,
                "navn" to sf.navn,
                "stonadsklasse" to sf.stønadsklasse.name,
                "sakstype" to "S",
                "resultat" to sf.resultat.name,
                "referansenummer" to sf.referansenummer,
                "kilde" to sf.kilde,
                "processed" to "N",
                "oppdatertAv" to Configuration.application["OEBS_BRUKER_ID"],
                "jobId" to -1
            )).asUpdate)
        }
    }
}
