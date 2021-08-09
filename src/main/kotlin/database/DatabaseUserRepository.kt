package database

import FollowStatus
import Position
import User
import UserRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class DatabaseUserRepository : UserRepository {
    private var conn: Connection? = null
    private var statementRunner: StatementRunner? = null
    private var userIdToUser: MutableMap<String, User>? = null

    init {
        try {
            val conn = DatabaseConnection.get()
            createTables()
            statementRunner = StatementRunner(conn)
        } catch (e: SQLException) {
            throw IllegalStateException(e)
        }
        userIdToUser = loadFromDatabase()
    }

    @Throws(SQLException::class)
    private fun createTables() {
        conn!!.createStatement()
            .executeUpdate(
                "CREATE TABLE IF NOT EXISTS " +
                        "users (" +
                        "id VARCHAR(15) NOT NULL," +
                        "password VARBINARY(20) NOT NULL," +
                        "salt VARBINARY(16) NOT NULL," +
                        "position INT NOT NULL" +
                        ")"
            )
        conn!!.createStatement()
            .executeUpdate(
                ("CREATE TABLE IF NOT EXISTS " +
                        "followers (" +
                        "follower VARCHAR(15) NOT NULL," +
                        "userToFollow VARCHAR(15) NOT NULL" +
                        ")")
            )
    }

    private fun loadFromDatabase(): MutableMap<String, User> {
        val users = HashMap<String, User>()
        statementRunner!!.query("SELECT id, password, salt, position FROM users", object : With<ResultSet> {
            override fun run(stmt: ResultSet) {
                val id = stmt.getString(ID)
                val password = stmt.getBytes(PASSWORD)
                val salt = stmt.getBytes(SALT)
                val position = Position(stmt.getInt(DatabaseUserRepository.POSITION))
                val user = User(id, password, salt, position)
                users[id] = user
            }
        })
        return users
    }

    override fun add(user: User): Boolean {
        val userId = user.id
        val success = userIdToUser?.putIfAbsent(userId, user) == null
        if (success) {
            statementRunner!!.withStatement(
                "INSERT INTO users (id, password, salt, position) VALUES (?,?,?,?)", object : With<PreparedStatement> {
                    override fun run(stmt: PreparedStatement) {
                        stmt.setString(ID, userId)
                        stmt.setBytes(PASSWORD, user.password)
                        stmt.setBytes(SALT, user.salt)
                        stmt.setInt(POSITION, user.lastSeenPosition.value)
                        stmt.executeUpdate()
                    }
                })
        }
        return success
    }


    override fun get(userId: String): User? {
        return userIdToUser?.get(userId)
    }

    override fun update(user: User) {
        statementRunner?.withStatement(
            "UPDATE users SET position=? WHERE id=?", object : With<PreparedStatement> {
                override fun run(stmt: PreparedStatement) {
                    stmt.setInt(1, user.lastSeenPosition.value)
                    stmt.setString(2, user.id)
                    stmt.executeUpdate()
                }
            })
    }

    override fun clear() {
        statementRunner!!.update("delete from users")
        statementRunner!!.update("delete from followers")
        userIdToUser!!.clear()
    }

    override fun follow(follower: User, userToFollow: User): FollowStatus {
        val followStatus = userToFollow.addFollower(follower)
        if (followStatus == FollowStatus.SUCCESS) {
            statementRunner!!.withStatement(
                "INSERT INTO followers (follower, userToFollow) VALUES (?,?)", object : With<PreparedStatement> {
                    override fun run(stmt: PreparedStatement) {
                        stmt.setString(FOLLOWER, follower.id);
                        stmt.setString(USER_TO_FOLLOW, userToFollow.id);
                        stmt.executeUpdate();
                    }
                })
        }
        return followStatus
    }

    override fun close() {
        conn?.close();
    }

    companion object {
        val ID = 1
        val PASSWORD = 2
        val SALT = 3
        val POSITION = 4
        val FOLLOWER = 1
        val USER_TO_FOLLOW = 2
    }

}