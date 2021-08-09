package database

import Position
import Twoot
import TwootQuery
import TwootRepository
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.function.Consumer
import java.util.stream.Collectors

class DatabaseTwootRepository : TwootRepository {
    private var position = Position.INITIAL_POSITION
    private var statementRunner: StatementRunner

    init {
        try {
            val conn = DatabaseConnection.get()
            conn.createStatement()
                .executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " +
                            "twoots (" +
                            "position INT IDENTITY," +
                            "id VARCHAR(36) UNIQUE NOT NULL," +
                            "senderId VARCHAR(15) NOT NULL," +
                            "content VARCHAR(140) NOT NULL" +
                            ")"
                )
            statementRunner = StatementRunner(conn)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun add(id: String, userId: String, content: String): Twoot {
        statementRunner.withStatement(
            "INSERT INTO twoots (id, senderId, content) VALUES (?,?, ?)",
            object : With<PreparedStatement> {
                override fun run(stmt: PreparedStatement) {
                    stmt.setString(1, id)
                    stmt.setString(2, userId)
                    stmt.setString(3, content)
                    stmt.executeUpdate()
                    val rs: ResultSet = stmt.getGeneratedKeys()
                    if (rs.next()) {
                        position = Position(rs.getInt(1))
                    }
                }
            }
        )
        return Twoot(id, userId, content, position)
    }

    override fun get(id: String): Twoot? {
        return statementRunner.extract(
            "SELECT * FROM twoots WHERE id = ?", object : Extractor<Twoot?> {
                override fun run(statement: PreparedStatement): Twoot? {
                    statement.setString(1, id)
                    val resultSet = statement.executeQuery()
                    return if (resultSet.next()) {
                        extractTwoot(resultSet)
                    } else {
                        null
                    }
                }
            })
    }

    @Throws(SQLException::class)
    private fun extractTwoot(rs: ResultSet): Twoot {
        val position = Position(rs.getInt(1))
        val id = rs.getString(2)
        val senderId = rs.getString(3)
        val content = rs.getString(4)
        return Twoot(id, senderId, content, position)
    }

    override fun delete(twoot: Twoot) {
        statementRunner.withStatement(
            "DELETE FROM twoots WHERE position = ?", object : With<PreparedStatement> {
                override fun run(stmt: PreparedStatement) {
                    stmt.setInt(1, position.value)
                    stmt.executeUpdate()
                }
            })
    }

    private fun usersTupleLoop(following: Set<String>): String? {
        val quotedIds: MutableList<String> = ArrayList()
        for (id in following) {
            quotedIds.add("'$id'")
        }
        return '('.toString() + java.lang.String.join(",", quotedIds) + ')'
    }

    private fun usersTuple(following: Set<String>): String? {
        return following
            .stream()
            .map { id: String -> "'$id'" }
            .collect(Collectors.joining(",", "(", ")"))
    }

    override fun query(twootQuery: TwootQuery, callback: Consumer<Twoot>) {
        if (!twootQuery.hasUsers()) {
            return
        }

        val lastSeenPosition = twootQuery.lastSeenPosition
        val inUsers = twootQuery.inUsers

        statementRunner.query(
            "SELECT * " +
                    "FROM   twoots " +
                    "WHERE  senderId IN " + usersTuple(inUsers) +
                    "AND    twoots.position > " + lastSeenPosition.value,
            object : With<ResultSet> {
                override fun run(stmt: ResultSet) {
                    callback.accept(extractTwoot(stmt))
                }
            })
    }

    override fun clear() {
        statementRunner.update("delete from twoots")
        statementRunner.update("DROP SCHEMA PUBLIC CASCADE")
    }
}