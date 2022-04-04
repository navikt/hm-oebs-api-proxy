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
        @Language("OracleSQL")
        var query =
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

        return sessionOf(dataSource).use { it ->
            it.run(
                queryOf(query, hmsnr).map { row ->
                    LagerStatus(
                        erPåLager = (
                            row.int("fysisk") +
                                row.int("bestillinger") +
                                row.int("anmodning") +
                                row.int("intanmodning")
                            ) >= (
                            + row.int("behovsmeldt") +
                                row.int("reservert") +
                                row.int("restordre")
                            ),

                        organisasjons_id = row.int("organisasjons_id"),
                        organisasjons_navn = row.string("organisasjons_navn"),
                        artikkelnummer = row.string("artikkelnummer"),
                        artikkelid = row.int("artikkelid"),
                        fysisk = row.int("fysisk"),
                        tilgjengeligatt = row.int("tilgjengeligatt"),
                        tilgjengeligroo = row.int("tilgjengeligroo"),
                        tilgjengelig = row.int("tilgjengelig"),
                        behovsmeldt = row.int("behovsmeldt"),
                        reservert = row.int("reservert"),
                        restordre = row.int("restordre"),
                        bestillinger = row.int("bestillinger"),
                        anmodning = row.int("anmodning"),
                        intanmodning = row.int("intanmodning"),
                        forsyning = row.int("forsyning"),
                        sortiment = row.string("sortiment").lowercase().trim() == "ja",
                        lagervare = row.string("lagervare").lowercase().trim() == "ja",
                        minmax = row.string("minmax").lowercase().trim() == "ja",
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
