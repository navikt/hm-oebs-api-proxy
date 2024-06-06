package no.nav.hjelpemidler.database

import kotliquery.Row
import org.intellij.lang.annotations.Language

typealias QueryParameters = Map<String, Any?>
typealias RowMapper<T> = (Row) -> T

interface JdbcOperations {
    fun <T : Any> single(
        @Language("Oracle") sql: String,
        queryParameters: QueryParameters = emptyMap(),
        mapper: RowMapper<T>,
    ): T

    fun <T : Any> singleOrNull(
        @Language("Oracle") sql: String,
        queryParameters: QueryParameters = emptyMap(),
        mapper: RowMapper<T>,
    ): T?

    fun <T : Any> list(
        @Language("Oracle") sql: String,
        queryParameters: QueryParameters = emptyMap(),
        mapper: RowMapper<T>,
    ): List<T>

    fun <T : Any> list(
        @Language("Oracle") sql: String,
        vararg queryParameters: Any?,
        mapper: RowMapper<T>,
    ): List<T>

    fun update(
        @Language("Oracle") sql: String,
        queryParameters: QueryParameters = emptyMap(),
    ): Int

    fun execute(
        @Language("Oracle") sql: String,
        queryParameters: QueryParameters = emptyMap(),
    ): Boolean
}
