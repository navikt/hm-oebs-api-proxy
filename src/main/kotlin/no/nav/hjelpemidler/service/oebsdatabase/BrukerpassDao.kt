package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class BrukerpassDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun brukerpassForFnr(fnr: String): Brukerpass {
        @Language("OracleSQL")
        var query =
            """
                SELECT KONTRAKT_NUMMER, SJEKK_NAVN, START_DATE, END_DATE
                FROM apps.XXRTV_DIGIHOT_OEBS_BRUKERP_V
                WHERE FNR = ?
            """.trimIndent()

        return sessionOf(dataSource).use { it ->
            it.run(
                queryOf(query, fnr).map { row ->
                    Brukerpass(
                        brukerpass = true,
                        kontraktNummer = row.stringOrNull("KONTRAKT_NUMMER"),
                        row.localDateOrNull("START_DATE"),
                        row.localDateOrNull("END_DATE")
                    )
                }.asSingle
            )
        } ?: Brukerpass(brukerpass = false)
    }

    var brukerpassRollerMedByttbareHjelpemidlerRes: List<Brukerpassrollebytter>? = null

    fun brukerpassRollerMedByttbareHjelpemidler(): List<Brukerpassrollebytter> {
        /*if (brukerpassRollerMedByttbareHjelpemidlerRes != null) {
            return brukerpassRollerMedByttbareHjelpemidlerRes!!
        }*/

        logg.info { "Gjør spørring brukerpassRollerMedByttbareHjelpemidler..." }
        @Language("OracleSQL")
        var query =
            """
                SELECT a.FNR, b.UTLÅNS_TYPE, b.INNLEVERINGSDATO, b.OPPDATERT_INNLEVERINGSDATO
                FROM apps.XXRTV_DIGIHOT_OEBS_BRUKERP_V a
                INNER JOIN apps.XXRTV_DIGIHOT_HJM_UTLAN_FNR_V b ON a.FNR = b.FNR
                AND (b.KATEGORI3_NUMMER = '123903' OR b.KATEGORI3_NUMMER = '090312')
                AND (b.UTLÅNS_TYPE = 'P' OR b.UTLÅNS_TYPE = 'F')
            """.trimIndent()

        val resultat: List<Brukerpassrollebytter> = sessionOf(dataSource).use { it ->
            it.run(
                queryOf(query).map { row ->
                    Brukerpassrollebytter(
                        fnr = row.string("FNR"),
                        utlånsType = row.stringOrNull("UTLÅNS_TYPE"),
                        innleveringsDato = row.stringOrNull("INNLEVERINGSDATO"),
                        oppdatertInnleveringsDato = row.stringOrNull("OPPDATERT_INNLEVERINGSDATO"),
                    )
                }.asList
            )
        }

        logg.info {
            """
            brukerpassRollerMedByttbareHjelpemidler resultat:
            antall: ${resultat.size}
            antall unike fnr: ${resultat.map { it.fnr }.distinct().size}
            antall kanByttes: ${resultat.filter { it.kanByttes }.size}
            duplikate fnr: ${resultat.groupBy { it.fnr }.filter { it.value.size > 1 }}
        """.trimIndent()
        }

        brukerpassRollerMedByttbareHjelpemidlerRes = resultat
            .filter { it.kanByttes }
            .distinctBy { it.fnr }

        logg.info { "endelig resultat: ${brukerpassRollerMedByttbareHjelpemidlerRes!!.size} stk" }

        return brukerpassRollerMedByttbareHjelpemidlerRes!!
    }

    data class Brukerpassrollebytter(
        val fnr: String,
        val utlånsType: String?,
        val innleveringsDato: String?,
        val oppdatertInnleveringsDato: String?,
    ) {
        val kanByttes = erPermanentUtlån(utlånsType) || erGyldigTidsbestemtUtlån(
            oppdatertInnleveringsDato,
            innleveringsDato,
            utlånsType
        )
    }
}

data class Brukerpass(
    val brukerpass: Boolean,
    val kontraktNummer: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

fun testHelper(row: Row) {
    logg.info("DEBUG: Row start:")
    val c = row.underlying.metaData.columnCount
    for (i in 1..c) {
        logg.info("DEBUG: Column #$i")
        val label = row.underlying.metaData.getColumnLabel(i)
        val name = row.underlying.metaData.getColumnName(i)
        val type = row.underlying.metaData.getColumnTypeName(i)
        logg.info("DEBUG: Column #$i - $label/$name - $type")
    }
    logg.info("DEBUG: Row end.")
}
