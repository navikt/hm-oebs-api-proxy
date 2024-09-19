package no.nav.hjelpemidler.database

import com.zaxxer.hikari.HikariDataSource
import no.nav.hjelpemidler.service.BrukernummerDao
import no.nav.hjelpemidler.service.BrukerpassDao
import no.nav.hjelpemidler.service.HjelpemiddeloversiktDao
import no.nav.hjelpemidler.service.LagerDao
import no.nav.hjelpemidler.service.PersoninformasjonDao
import no.nav.hjelpemidler.service.ServiceforespørselDao
import no.nav.hjelpemidler.service.TittelForHmsnrDao
import java.io.Closeable
import javax.sql.DataSource

class Database(private val dataSource: DataSource) : Closeable {
    suspend fun <T> transaction(block: DaoProvider.() -> T): T = transactionAsync(dataSource) {
        DaoProvider(it).block()
    }

    suspend fun isValid(): Boolean = withDatabaseContext {
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
        val tittelForHmsnrDao = TittelForHmsnrDao(tx)
    }
}
