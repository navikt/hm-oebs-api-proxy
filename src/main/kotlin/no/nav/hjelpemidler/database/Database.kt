package no.nav.hjelpemidler.database

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun isValid(): Boolean = withContext(Dispatchers.IO) {
        dataSource.connection.use { it.isValid(10) }
    }

    suspend fun testView(schema: String, view: String): Boolean = transactionAsync(dataSource) { tx ->
        tx.single(
            """
            SELECT EXISTS (
                SELECT 1
                FROM information_schema.views v
                WHERE 
                    v.table_schema = :schemaName
                    AND v.table_name = :tableName
                    AND has_table_privilege(
                        quote_ident(v.table_schema) || '.' || quote_ident(v.table_name), 'SELECT'
                    )
            ) AS is_view_selectable;
            """.trimIndent(),
            mapOf(
                "schemaName" to schema,
                "tableName" to view,
            ),
        ) { row ->
            row.boolean("is_view_selectable")
        }
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
