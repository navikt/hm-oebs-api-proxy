package no.nav.hjelpemidler.database

import no.nav.hjelpemidler.database.Database.DaoProvider

suspend fun <T> testTransaction(block: DaoProvider.() -> T): T {
    val database = createDataSource {
        val parameters = mapOf(
            "MODE" to "Oracle",
            "DEFAULT_NULL_ORDERING" to "HIGH",
            "INIT" to "RUNSCRIPT FROM 'classpath:/oebsl.sql'",
        ).map { (key, value) -> "$key=$value" }.joinToString(";")
        jdbcUrl = "jdbc:h2:mem:oebsl;$parameters"
    }.let(::Database)
    return database.use { it.transaction(block) }
}
