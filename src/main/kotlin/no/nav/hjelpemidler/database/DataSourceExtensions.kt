package no.nav.hjelpemidler.database

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

typealias QueryParameters = Map<String, Any?>

private fun <T> DataSource.withSession(block: (Session) -> T): T =
    sessionOf(this, strict = true).use(block)

fun <T> DataSource.single(
    @Language("Oracle") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: (Row) -> T,
): T = withSession {
    it.run(queryOf(sql, queryParameters).map(mapper).asSingle)
} ?: throw NoSuchElementException("Ingen treff i databasen!")

fun <T> DataSource.singleOrNull(
    @Language("Oracle") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: (Row) -> T,
): T? = withSession {
    it.run(queryOf(sql, queryParameters).map(mapper).asSingle)
}

fun <T> DataSource.list(
    @Language("Oracle") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: (Row) -> T,
): List<T> = withSession {
    it.run(queryOf(sql, queryParameters).map(mapper).asList)
}

fun <T> DataSource.list(
    @Language("Oracle") sql: String,
    vararg queryParameters: Any?,
    mapper: (Row) -> T,
): List<T> = withSession {
    it.run(queryOf(sql, *queryParameters).map(mapper).asList)
}

fun DataSource.update(
    @Language("Oracle") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): Int = withSession {
    it.run(queryOf(sql, queryParameters).asUpdate)
}

fun DataSource.execute(
    @Language("Oracle") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): Boolean = withSession {
    it.run(queryOf(sql, queryParameters).asExecute)
}
