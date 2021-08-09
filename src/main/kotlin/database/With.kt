package database

internal interface With<P> {
    fun run(stmt: P)
}