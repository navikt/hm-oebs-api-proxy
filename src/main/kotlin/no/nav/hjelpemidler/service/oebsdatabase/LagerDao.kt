package no.nav.hjelpemidler.service.oebsdatabase

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.lagerstatus.KommuneOppslag
import org.intellij.lang.annotations.Language

class LagerDao(private val tx: JdbcOperations) {
    private val kommuneOppslag by lazy(::KommuneOppslag)

    fun hentLagerstatus(hmsnr: String): List<Lagerstatus> {
        return hentLagerstatus(hmsnr, null)
    }

    fun hentLagerstatusForSentral(kommunenummer: String, hmsnr: String): Lagerstatus? {
        val orgNavn = kommuneOppslag.hentOrgNavn(kommunenummer) ?: return null
        return hentLagerstatus(hmsnr, orgNavn).firstOrNull()
    }

    private fun hentLagerstatus(hmsnr: String, orgNavn: String? = null): List<Lagerstatus> {
        @Language("Oracle")
        var sql = """
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
            FROM apps.XXRTV_DIGIHOT_UTVID_ART_V
            WHERE artikkelnummer = :hmsnr
        """.trimIndent()

        if (orgNavn != null) {
            sql += " AND organisasjons_navn = :orgNavn"
        }

        return tx.list(
            sql,
            mapOf("hmsnr" to hmsnr, "orgNavn" to orgNavn),
        ) { row ->
            Lagerstatus(
                erPåLager = (
                    (row.intOrNull("fysisk") ?: 0) +
                        (row.intOrNull("bestillinger") ?: 0) +
                        (row.intOrNull("anmodning") ?: 0) +
                        (row.intOrNull("intanmodning") ?: 0)
                    ) > (
                    (row.intOrNull("behovsmeldt") ?: 0) +
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
        }
    }
}

data class Lagerstatus(
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
