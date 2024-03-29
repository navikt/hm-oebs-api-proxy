package no.nav.hjelpemidler.database

object OracleTestHelper {
    val dataSource by lazy {
        createDataSource {
            val parameters = mapOf(
                "MODE" to "Oracle",
                "DEFAULT_NULL_ORDERING" to "HIGH",
                "INIT" to "RUNSCRIPT FROM 'classpath:/oebsl.sql'",
            ).map { (key, value) -> "$key=$value" }.joinToString(";")
            jdbcUrl = "jdbc:h2:mem:oebsl;$parameters"
        }
    }
}
