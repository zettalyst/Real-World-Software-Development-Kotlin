package database

import java.sql.PreparedStatement
import java.sql.SQLException

interface Extractor<R> {
    @Throws(SQLException::class)
    fun run(statement: PreparedStatement): R
}