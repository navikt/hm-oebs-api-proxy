package no.nav.hjelpemidler.database

import no.nav.hjelpemidler.database.Database.DaoProvider

suspend fun <T> testTransaction(block: DaoProvider.() -> T): T =
    createDataSource(H2) {
        mode = H2Mode.ORACLE
        initScript = "/oebsl.sql"
    }.let(::Database).use { it.transaction(block) }
