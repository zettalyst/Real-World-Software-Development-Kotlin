package database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {
    companion object {
        @Throws(SQLException::class)
        fun get(): Connection {
            return DriverManager.getConnection("jdbc:hsqldb:db/mydatabase", "SA", "")
        }
    }

    init {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver")
        } catch (e: ClassNotFoundException) {
            throw Error(e)
        }
    }
}