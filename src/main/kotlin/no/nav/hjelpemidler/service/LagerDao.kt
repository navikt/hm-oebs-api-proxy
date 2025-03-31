package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.database.sql.Sql

class LagerDao(private val tx: JdbcOperations) {
    fun hentLagerstatus(hmsnr: String): List<Lagerstatus> = hentLagerstatus(listOf(hmsnr), null)

    fun hentLagerstatusForSentral(enhetNavn: String, hmsnr: String): Lagerstatus? = hentLagerstatus(listOf(hmsnr), enhetNavn).firstOrNull()

    fun hentLagerstatusForSentral(enhetNavn: String, hmsnrs: List<String>): List<Lagerstatus>? = hentLagerstatus(hmsnrs, enhetNavn)

    private fun hentLagerstatus(hmsnrs: List<String>, orgNavn: String? = null): List<Lagerstatus> {
        var indexedHmsnrs = hmsnrs.withIndex()
        var sql = Sql(
            """
                SELECT organisasjons_id,
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
                FROM apps.xxrtv_digihot_utvid_art_v
                WHERE artikkelnummer IN (${indexedHmsnrs.joinToString(",") { (index, _) -> ":hmsnr_$index" }})
            """.trimIndent(),
        )

        if (orgNavn != null) {
            sql = Sql("$sql AND organisasjons_navn = :orgNavn")
        }

        return tx.list(
            sql,
            mapOf("orgNavn" to orgNavn) + indexedHmsnrs.map { (index, hmsnr) -> "hmsnr_$index" to hmsnr },
        ) { row ->
            val antallPåLager = (
                row.intOrZero("fysisk") +
                    row.intOrZero("bestillinger") +
                    row.intOrZero("anmodning") +
                    row.intOrZero("intanmodning") -
                    row.intOrZero("behovsmeldt") -
                    row.intOrZero("reservert") -
                    row.intOrZero("restordre")
                )

            Lagerstatus(
                antallPåLager = antallPåLager,
                erPåLager = antallPåLager > 0,

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
        }
    }
}

private fun Row.intOrZero(columnLabel: String): Int = this.intOrNull(columnLabel) ?: 0

data class Lagerstatus(
    val antallPåLager: Int,
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
