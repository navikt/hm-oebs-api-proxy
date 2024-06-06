package no.nav.hjelpemidler.database

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotliquery.sessionOf
import no.nav.hjelpemidler.service.oebsdatabase.BrukernummerDao
import no.nav.hjelpemidler.service.oebsdatabase.BrukerpassDao
import no.nav.hjelpemidler.service.oebsdatabase.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.oebsdatabase.LagerDao
import no.nav.hjelpemidler.service.oebsdatabase.PersoninformasjonDao
import no.nav.hjelpemidler.service.oebsdatabase.TittelForHmsnrDao
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselDao
import no.nav.hjelpemidler.serviceforespørsel.ServiceforespørselFeilDao
import java.io.Closeable
import javax.sql.DataSource

class Database(private val dataSource: DataSource) : Closeable {
    suspend fun <T> transaction(block: DaoProvider.() -> T): T = withContext(Dispatchers.IO) {
        sessionOf(dataSource, strict = true).use { session ->
            session.transaction { block(DaoProvider(SessionJdbcOperations(it))) }
        }
    }

    suspend fun isValid(): Boolean = withContext(Dispatchers.IO) {
        dataSource.connection.use { it.isValid(10) }
    }

    val isClosed: Boolean get() = dataSource is HikariDataSource && dataSource.isClosed

    override fun close() {
        if (dataSource is Closeable) dataSource.close()
    }

    class DaoProvider(tx: JdbcOperations) {
        val brukernummerDao = BrukernummerDao(tx)
        val brukerpassDao = BrukerpassDao(tx)
        val hjelpemiddeloversiktDao = HjelpemiddeloversiktDao(tx)
        val lagerDao = LagerDao(tx)
        val personinformasjonDao = PersoninformasjonDao(tx)
        val serviceforespørselDao = ServiceforespørselDao(tx)
        val serviceforespørselFeilDao = ServiceforespørselFeilDao(tx)
        val tittelForHmsnrDao = TittelForHmsnrDao(tx)
    }
}
