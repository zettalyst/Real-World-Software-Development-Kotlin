import java.util.*

class TwootQuery {
    lateinit var inUsers: Set<String>
    lateinit var lastSeenPosition: Position

    fun inUsers(inUsers: Set<String>): TwootQuery {
        this.inUsers = inUsers
        return this
    }

    fun inUsers(vararg inUsers: String?): TwootQuery {
        return inUsers(java.util.HashSet(Arrays.asList(*inUsers)))
    }

    fun lastSeenPosition(lastSeenPosition: Position): TwootQuery {
        this.lastSeenPosition = lastSeenPosition
        return this
    }

    fun hasUsers(): Boolean {
        return inUsers != null && !inUsers!!.isEmpty()
    }
}