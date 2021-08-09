package in_memory

import Position
import Twoot
import TwootQuery
import TwootRepository
import java.util.function.Consumer
import kotlin.collections.ArrayList

class InMemoryTwootRepository : TwootRepository {
    private val twoots: MutableList<Twoot> = ArrayList()
    private var currentPosition: Position = Position.INITIAL_POSITION

    override fun query(twootQuery: TwootQuery, callback: Consumer<Twoot>) {
        if (!twootQuery.hasUsers()) {
            return
        }

        val lastSeenPosition = twootQuery.lastSeenPosition
        val inUsers = twootQuery.inUsers

        twoots
            .stream()
            .filter { inUsers.contains(it.senderId) }
            .filter { it.isAfter(lastSeenPosition) }
            .forEach(callback)
    }

    fun queryLoop(twootQuery: TwootQuery, callback: Consumer<Twoot>) {
        if (!twootQuery.hasUsers()) {
            return
        }
        val lastSeenPosition = twootQuery.lastSeenPosition
        val inUsers = twootQuery.inUsers
        for (twoot in twoots) {
            if (inUsers.contains(twoot.senderId) && twoot.isAfter(lastSeenPosition)) {
                callback.accept(twoot)
            }
        }
    }

    override fun get(id: String): Twoot? {
        return twoots.firstOrNull { it.id == id }
    }

    override fun delete(twoot: Twoot) {
        twoots.remove(twoot)
    }

    override fun add(id: String, userId: String, content: String): Twoot {
        currentPosition = currentPosition.next()

        val twootPosition = currentPosition
        val twoot = Twoot(id, userId, content, twootPosition)
        twoots.add(twoot)
        return twoot
    }

    override fun clear() {
        twoots.clear()
    }
}