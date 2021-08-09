package database

import java.sql.*

internal class StatementRunner(private val conn: Connection) {
    fun <R> extract(sql: String, extractor: Extractor<R>): R {
        try {
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.clearParameters()
            return extractor.run(stmt)
        } catch (e: SQLException) {
            throw IllegalStateException(e)
        }
    }

    fun withStatement(sql: String, withPreparedStatement: With<PreparedStatement>) {
        extract(sql, object : Extractor<Unit> {
            override fun run(statement: PreparedStatement) {
                withPreparedStatement.run(statement)
            }
        })
    }

    fun update(sql: String) {
        withStatement(sql, object : With<PreparedStatement> {
            override fun run(stmt: PreparedStatement) {
                stmt.execute()
            }
        })
    }

    fun query(sql: String, withPreparedStatement: With<ResultSet>) {
        withStatement(sql, object : With<PreparedStatement> {
            override fun run(stmt: PreparedStatement) {
                val resultSet = stmt.executeQuery()
                while (resultSet.next()) {
                    withPreparedStatement.run(resultSet)
                }
            }
        })
    }
}