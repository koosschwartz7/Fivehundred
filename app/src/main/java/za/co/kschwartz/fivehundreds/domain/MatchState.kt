package za.co.kschwartz.fivehundreds.domain

enum class MatchState(val state: String) {
    LOBBY("LOBBY"),
    IN_PROGRESS("IN_PROGRESS"),
    FINISHED("FINISHED")
}