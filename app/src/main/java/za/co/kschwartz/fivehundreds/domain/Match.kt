package za.co.kschwartz.fivehundreds.domain

import za.co.kschwartz.fivehundreds.MatchException

class Match {

    var teams = mutableMapOf<Int,Team>()
    var rounds = arrayListOf<Round>()

    init {
        teams.clear();
        teams.put(1,Team(1))
        teams.put(2,Team(2))
    }

    fun joinPlayer(player: Player, teamNr: Int): Boolean {
        val team: Team? = teams[teamNr]
        if (team != null) {
            return team.addPlayer(player)
        }
        return false
    }

    fun playNewRound(): Round {
        if (teams[1]!!.players.size < 2 || teams[2]!!.players.size < 2) {
            throw MatchException("Match requires four players to start.")
        }

        var round: Round = Round(teams[1]!!.players[1]!!,
            teams[2]!!.players[1]!!, teams[1]!!.players[2]!!, teams[2]!!.players[2]!!, rounds.size, getInitialBettingPlayerIndex())

        return round;
    }

    //TODO: Test
    private fun getInitialBettingPlayerIndex():Int {
        var initialBettingPlayerIndex = 0
        if (rounds.size > 0) {
            initialBettingPlayerIndex = rounds.last().initialBettingPlayerIndex + 1
            if (initialBettingPlayerIndex >= 4) {
                initialBettingPlayerIndex = 0
            }
        }

        return initialBettingPlayerIndex
    }

}