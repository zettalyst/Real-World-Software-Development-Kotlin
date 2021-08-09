import java.util.function.Consumer

interface TwootRepository {
    fun add(id: String, userId: String, content: String): Twoot
    fun get(id: String): Twoot?
    fun delete(twoot: Twoot)
    fun query(twootQuery: TwootQuery, callback: Consumer<Twoot>)
    fun clear()
}