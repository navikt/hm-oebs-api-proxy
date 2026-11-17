package no.nav.hjelpemidler.service

import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.sql.Sql
import no.nav.hjelpemidler.models.Utlån
import no.nav.hjelpemidler.models.UtlånForKommuneApi
import no.nav.hjelpemidler.models.UtlånMedProduktinfo
import no.nav.hjelpemidler.models.UtlånMedSerienr
import no.nav.hjelpemidler.models.tilLocalDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HjelpemiddeloversiktDao(private val tx: JdbcOperations) {
    fun hentHjelpemiddeloversikt(fnr: String): List<UtlånMedProduktinfo> {
        val query = Sql(
            """
                SELECT antall,
                       enhet,
                       kategori3_nummer,
                       kategori3_beskrivelse,
                       artikkel_beskrivelse,
                       artikkelnummer,
                       serie_nummer,
                       utlåns_dato,
                       ordre_nummer,
                       artikkelstatus,
                       utlåns_type,
                       innleveringsdato,
                       oppdatert_innleveringsdato
                FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
                WHERE fnr = :fnr
                ORDER BY utlåns_dato DESC
            """.trimIndent(),
        )

        val hjelpemiddeloversikt = tx.list(query, mapOf("fnr" to fnr)) { row ->
            UtlånMedProduktinfo(
                antall = row.string("antall"),
                antallEnhet = row.string("enhet"),
                kategoriNummer = row.string("kategori3_nummer"),
                kategori = row.string("kategori3_beskrivelse"),
                artikkelBeskrivelse = row.string("artikkel_beskrivelse"),
                artikkelNr = row.string("artikkelnummer"),
                serieNr = row.stringOrNull("serie_nummer"),
                datoUtsendelse = row.stringOrNull("utlåns_dato"),
                ordrenummer = row.stringOrNull("ordre_nummer"),
                artikkelStatus = row.string("artikkelstatus"),
                utlånsType = row.stringOrNull("utlåns_type"),
                innleveringsdato = row.stringOrNull("innleveringsdato"),
                oppdatertInnleveringsdato = row.stringOrNull("oppdatert_innleveringsdato"),
            )
        }.toMutableList()

        // Mocks i dev
        if (!Environment.current.isProd && fnr in setOf("26848497710", "15084300133", "03847797958")) {
            hjelpemiddeloversikt.add(
                UtlånMedProduktinfo(
                    antall = "1",
                    antallEnhet = "STK",
                    kategoriNummer = "123903",
                    kategori = "Mobilitets- og markeringsstokker (hvite stokker)",
                    artikkelBeskrivelse = "Mobilitetsstokk med to sensorer. Den ene sensoren peker frem og ned, den andre skrått oppover. Stokken gir tilbakemelding om hindringer fra bakkeplan til over hodehøyde. Denne gis som vibrasjon i to knapper på håndtaket. Innstillinger for rekkevidde tilpasset inne- og utebruk. Stokken er sammenleggbar og fås i ulike lengder fra 110 cm til 150 cm.",
                    artikkelNr = "265940",
                    serieNr = null,
                    datoUtsendelse = hjelpemiddeloversikt.lastOrNull()?.datoUtsendelse ?: LocalDate.now().minusDays(30)
                        .toString(),
                    ordrenummer = "1001",
                    artikkelStatus = "",
                    utlånsType = "P",
                    innleveringsdato = null,
                    oppdatertInnleveringsdato = null,
                ),
            )
            hjelpemiddeloversikt.add(
                UtlånMedProduktinfo(
                    antall = "1",
                    antallEnhet = "STK",
                    kategoriNummer = "123903",
                    kategori = "Mobilitets- og markeringsstokker (hvite stokker)",
                    artikkelBeskrivelse = "Lett mobilitystokk i komposittmateriale. Stokken har fem ledd og leveres med standard tupp. Mange ulike tupper kan leveres. Stokken finnes i lengde fra 90-160 cm i 5 cm intervaller.",
                    artikkelNr = "330587",
                    serieNr = null,
                    datoUtsendelse = hjelpemiddeloversikt.lastOrNull()?.datoUtsendelse ?: LocalDate.now().minusDays(31)
                        .toString(),
                    ordrenummer = "1000",
                    artikkelStatus = "",
                    utlånsType = "P",
                    innleveringsdato = null,
                    oppdatertInnleveringsdato = null,
                ),
            )
        }

        return hjelpemiddeloversikt
    }

    fun hentHjelpemiddeloversiktForKommuneApi(fnr: String): List<UtlånForKommuneApi> {
        val query = Sql(
            """
                SELECT
                    artikkelnummer,
                    artikkel_beskrivelse,
                    serie_nummer,
                    antall,
                    enhet,
                    kategori3_nummer,
                    utlåns_dato,
                    installasjons_addresse,
                    installasjons_kommune,
                    installasjons_postnummer,
                    installasjons_by,
                    bosteds_addresse,
                    bosteds_kommune,
                    bosteds_postnummer,
                    bosteds_by
                FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
                WHERE fnr = :fnr
                ORDER BY utlåns_dato DESC
            """.trimIndent(),
        )

        return tx.list(query, mapOf("fnr" to fnr)) { row ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dt = row.stringOrNull("utlåns_dato")?.let {
                LocalDateTime.parse(it, formatter)
            }
            UtlånForKommuneApi(
                artikkelnr = row.string("artikkelnummer"),
                artikkelBeskrivelse = row.string("artikkel_beskrivelse"),
                serienr = row.stringOrNull("serie_nummer"),
                antall = row.string("antall"),
                antallEnhet = row.string("enhet"),
                isokategori = row.string("kategori3_nummer"),
                utlånsDato = dt,
                installasjonAdresse = row.stringOrNull("installasjons_addresse"),
                installasjonKommune = row.stringOrNull("installasjons_kommune"),
                installasjonPostnummer = row.stringOrNull("installasjons_postnummer"),
                installasjonBy = row.stringOrNull("installasjons_by"),
                bostedsAdresse = row.stringOrNull("bosteds_addresse"),
                bostedsKommune = row.stringOrNull("bosteds_kommune"),
                bostedsPostnummer = row.stringOrNull("bosteds_postnummer"),
                bostedsBy = row.stringOrNull("bosteds_by"),
            )
        }
    }

    fun utlånPåIsokode(fnr: String, isokode: String): List<UtlånPåIsokode> = tx.list(
        """
            SELECT kategori3_nummer, utlåns_dato
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE fnr = :fnr
              AND kategori3_nummer = :isokode
            ORDER BY utlåns_dato DESC
        """.trimIndent(),
        mapOf("fnr" to fnr, "isokode" to isokode),
    ) { row ->
        UtlånPåIsokode(
            kategoriNummer = row.string("kategori3_nummer"),
            datoUtsendelse = row.string("utlåns_dato"),
        )
    }

    fun utlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånMedSerienr? = tx.singleOrNull(
        """
            SELECT fnr, artikkelnummer, serie_nummer, utlåns_dato, opprettelsesdato, kategori3_nummer
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE artikkelnummer = :artnr
              AND serie_nummer = :serienr
            ORDER BY utlåns_dato DESC
            FETCH NEXT 1 ROW ONLY
        """.trimIndent(),
        mapOf("artnr" to artnr, "serienr" to serienr),
    ) { row ->
        UtlånMedSerienr(
            fnr = row.string("fnr"),
            artnr = row.string("artikkelnummer"),
            serienr = row.string("serie_nummer"),
            utlånsDato = row.string("utlåns_dato"),
            opprettetDato = row.stringOrNull("opprettelsesdato")?.tilLocalDate(),
            isokode = row.stringOrNull("kategori3_nummer"),
        )
    }

    fun utlånPåArtnrOgFødselsnr(artnr: String, fnr: String): List<Utlån> = tx.list(
        """
            SELECT fnr, serie_nummer, artikkelnummer,  utlåns_dato, opprettelsesdato, kategori3_nummer
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE artikkelnummer = :artnr
              AND fnr = :fnr
            ORDER BY utlåns_dato DESC
        """.trimIndent(),
        mapOf("artnr" to artnr, "fnr" to fnr),
    ) { row ->
        Utlån(
            fnr = row.string("fnr"),
            artnr = row.string("artikkelnummer"),
            serienr = row.stringOrNull("serie_nummer"),
            utlånsDato = row.string("utlåns_dato"),
            opprettetDato = row.stringOrNull("opprettelsesdato")?.tilLocalDate(),
            isokode = row.stringOrNull("kategori3_nummer"),
        )
    }

    fun utlånPåArtnr(artnr: String): List<UtlånMedSerienr> = tx.list(
        """
            SELECT fnr, artikkelnummer, serie_nummer, utlåns_dato, opprettelsesdato, kategori3_nummer
            FROM apps.xxrtv_digihot_hjm_utlan_fnr_v
            WHERE artikkelnummer = :artnr
        """.trimIndent(),
        mapOf("artnr" to artnr),
    ) { row ->
        UtlånMedSerienr(
            fnr = row.string("fnr"),
            artnr = row.string("artikkelnummer"),
            serienr = row.string("serie_nummer"),
            utlånsDato = row.string("utlåns_dato"),
            opprettetDato = row.stringOrNull("opprettelsesdato")?.tilLocalDate(),
            isokode = row.stringOrNull("kategori3_nummer"),
        )
    }

    data class UtlånPåIsokode(
        val kategoriNummer: String,
        val datoUtsendelse: String,
    )
}
