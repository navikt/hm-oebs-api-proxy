package no.nav.hjelpemidler.database

import no.nav.hjelpemidler.service.oebsdatabase.BrukernummerDao
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao
import no.nav.hjelpemidler.service.oebsdatabase.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.oebsdatabase.PersoninformasjonDao
import no.nav.hjelpemidler.service.oebsdatabase.TittelForHmsnrDao
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselDao
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselFeilDao
import java.io.Closeable
import javax.sql.DataSource

class Database(private val dataSource: DataSource) : Closeable {
    val brukernummerDao = BrukernummerDao(dataSource)
    val brukerpassDao = BrukerpassDao(dataSource)
    val hjelpemiddeloversiktDao = HjelpemiddeloversiktDao(dataSource)
    val personinformasjonDao = PersoninformasjonDao(dataSource)
    val serviceforespørselDao = ServiceforespørselDao(dataSource)
    val serviceforespørselFeilDao = ServiceforespørselFeilDao(dataSource)
    val tittelForHmsnrDao = TittelForHmsnrDao(dataSource)

    override fun close() {
        if (dataSource is Closeable) dataSource.close()
    }
}
