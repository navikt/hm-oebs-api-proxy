package no.nav.hjelpemidler.database

import no.nav.hjelpemidler.database.Database.DaoProvider
import javax.sql.DataSource

fun createTestDataSource(): DataSource = createDataSource(H2) {
    mode = H2Mode.ORACLE
    initScript = "/oebsl.sql"
}

suspend fun <T> testTransaction(block: DaoProvider.() -> T): T =
    Database(createTestDataSource()).use { it.transaction(block) }
