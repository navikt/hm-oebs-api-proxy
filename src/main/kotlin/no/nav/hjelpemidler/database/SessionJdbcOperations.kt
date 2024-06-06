package no.nav.hjelpemidler.database

import kotliquery.Session
import kotliquery.queryOf

class SessionJdbcOperations(private val session: Session) : JdbcOperations {
    override fun <T : Any> single(sql: String, queryParameters: QueryParameters, mapper: RowMapper<T>): T =
        singleOrNull(sql, queryParameters, mapper)
            ?: throw NoSuchElementException("Spørringen ga ingen treff i databasen")

    override fun <T : Any> singleOrNull(sql: String, queryParameters: QueryParameters, mapper: RowMapper<T>): T? =
        session.single(queryOf(sql, queryParameters), mapper)

    override fun <T : Any> list(sql: String, queryParameters: QueryParameters, mapper: RowMapper<T>): List<T> =
        session.list(queryOf(sql, queryParameters), mapper)

    override fun <T : Any> list(sql: String, vararg queryParameters: Any?, mapper: RowMapper<T>): List<T> =
        session.list(queryOf(sql, *queryParameters), mapper)

    override fun update(sql: String, queryParameters: QueryParameters): Int =
        session.update(queryOf(sql, queryParameters))

    override fun execute(sql: String, queryParameters: QueryParameters): Boolean =
        session.execute(queryOf(sql, queryParameters))
}
