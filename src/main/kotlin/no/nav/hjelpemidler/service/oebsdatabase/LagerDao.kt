package no.nav.hjelpemidler.service.oebsdatabase

import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.hjelpemidler.configuration.Configuration
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class LagerDao(private val dataSource: DataSource = Configuration.dataSource) {
    fun lagerStatus(hmsnr: String): List<LagerStatus> {
        return lagerStatusInner(hmsnr)
    }

    fun lagerStatusSentral(orgNavn: String, hmsnr: String): LagerStatus? {
        return lagerStatusInner(hmsnr, orgNavn).firstOrNull()
    }

    private fun lagerStatusInner(hmsnr: String, orgNavn: String? = null): List<LagerStatus> {
        @Language("OracleSQL")
        var sql =
            """
                SELECT
                    organisasjons_id,
                    organisasjons_navn,
                    artikkelnummer,
                    artikkelid,
                    fysisk,
                    tilgjengeligatt,
                    tilgjengeligroo,
                    tilgjengelig,
                    behovsmeldt,
                    reservert,
                    restordre,
                    bestillinger,
                    anmodning,
                    intanmodning,
                    forsyning,
                    sortiment,
                    lagervare,
                    minmax
                FROM XXRTV_DIGIHOT_UTVID_ART_SOK_V
                WHERE artikkelnummer = ?
            """.trimIndent()

        var query = queryOf(sql, hmsnr)
        if (orgNavn != null) {
            query = queryOf("$sql AND organisasjons_navn = ?", hmsnr, orgNavn)
        }

        return sessionOf(dataSource).use { it ->
            it.run(
                query.map { row ->
                    LagerStatus(
                        erPåLager = (
                            (row.intOrNull("fysisk") ?: 0) +
                                (row.intOrNull("bestillinger") ?: 0) +
                                (row.intOrNull("anmodning") ?: 0) +
                                (row.intOrNull("intanmodning") ?: 0)
                            ) > (
                            + (row.intOrNull("behovsmeldt") ?: 0) +
                                (row.intOrNull("reservert") ?: 0) +
                                (row.intOrNull("restordre") ?: 0)
                            ),

                        organisasjons_id = row.intOrNull("organisasjons_id") ?: -1,
                        organisasjons_navn = row.stringOrNull("organisasjons_navn") ?: "<ukjent>",
                        artikkelnummer = row.stringOrNull("artikkelnummer") ?: "<ukjent>",
                        artikkelid = row.intOrNull("artikkelid") ?: -1,
                        fysisk = row.intOrNull("fysisk") ?: 0,
                        tilgjengeligatt = row.intOrNull("tilgjengeligatt") ?: 0,
                        tilgjengeligroo = row.intOrNull("tilgjengeligroo") ?: 0,
                        tilgjengelig = row.intOrNull("tilgjengelig") ?: 0,
                        behovsmeldt = row.intOrNull("behovsmeldt") ?: 0,
                        reservert = row.intOrNull("reservert") ?: 0,
                        restordre = row.intOrNull("restordre") ?: 0,
                        bestillinger = row.intOrNull("bestillinger") ?: 0,
                        anmodning = row.intOrNull("anmodning") ?: 0,
                        intanmodning = row.intOrNull("intanmodning") ?: 0,
                        forsyning = row.intOrNull("forsyning") ?: 0,
                        sortiment = row.stringOrNull("sortiment")?.lowercase()?.trim() == "ja",
                        lagervare = row.stringOrNull("lagervare")?.lowercase()?.trim() == "ja",
                        minmax = row.stringOrNull("minmax")?.lowercase()?.trim() == "ja",
                    )
                }.asList
            )
        }
    }
}

data class LagerStatus(
    val erPåLager: Boolean,

    val organisasjons_id: Int,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val artikkelid: Int,
    val fysisk: Int,
    val tilgjengeligatt: Int,
    val tilgjengeligroo: Int,
    val tilgjengelig: Int,
    val behovsmeldt: Int,
    val reservert: Int,
    val restordre: Int,
    val bestillinger: Int,
    val anmodning: Int,
    val intanmodning: Int,
    val forsyning: Int,
    val sortiment: Boolean,
    val lagervare: Boolean,
    val minmax: Boolean,
)
