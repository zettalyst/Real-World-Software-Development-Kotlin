data class Position(val value: Int) {
    fun next(): Position {
        return Position(value + 1)
    }

    companion object {
        val INITIAL_POSITION = Position(-1)
    }
}