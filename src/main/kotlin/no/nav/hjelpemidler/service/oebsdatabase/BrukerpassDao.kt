package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import no.nav.hjelpemidler.configuration.Configuration.dataSource
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

class BrukerpassDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun brukerpassForFnr(fnr: String): Boolean? {
        @Language("OracleSQL")
        var query =
            """
                SELECT BRUKER_PASS
                FROM XXRTV_DIGIHOT_OEBS_BRUKERP_V
                WHERE FNR = ?
            """.trimIndent()

        logg.info("DEBUG: Making query:")

        return sessionOf(dataSource).use { it ->
            it.run(
                queryOf(query, fnr).map { row ->
                    row.string("BRUKER_PASS").trim() == "Y"
                }.asSingle
            )
        }
    }
}

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
