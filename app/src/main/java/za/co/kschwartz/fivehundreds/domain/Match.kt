package za.co.kschwartz.fivehundreds.domain

import za.co.kschwartz.fivehundreds.MatchException
import kotlin.random.Random

class Match {

    var teams = mutableMapOf<String,Team>()
    var rounds = arrayListOf<Round>()
    val CODELENGTH = 6
    var uniqueMatchCode = generateMatchCode()

    init {
        teams.clear();
        teams.put("Team 1",Team(1))
        teams.put("Team 2",Team(2))
    }

    fun joinPlayer(player: Player, teamNr: Int): Boolean {
        val team: Team? = teams["Team $teamNr"]
        if (team != null) {
            return team.addPlayer(player)
        }
        return false
    }

    fun playNewRound(): Round {
        if (teams["Team 1"]!!.players.size < 2 || teams["Team 2"]!!.players.size < 2) {
            throw MatchException("Match requires four players to start.")
        }

        var round: Round = Round(teams["Team 1"]!!.players[1]!!,
            teams["Team 2"]!!.players[1]!!, teams["Team 1"]!!.players[2]!!, teams["Team 2"]!!.players[2]!!, rounds.size, getInitialBettingPlayerIndex())
        rounds.add(round)
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

    fun generateMatchCode():String {
        val charPool : List<Char> = ('A'..'Z') + ('0'..'9')
        val rand = Random(System.nanoTime())
        var randomString = ""
        for (i in 1..CODELENGTH) {
            randomString += charPool[(charPool.indices).random(rand)]
        }
        uniqueMatchCode = randomString
        return uniqueMatchCode
    }

    fun getNextAvailablePlayerNr(): Int {
        var playerNr = 1
        for (i in 1..2) {
            for (j in 1..2) {
                if (teams["Team $i"]?.players?.get(j)?.playerNr  == 99) {
                    return playerNr
                } else {
                    playerNr++
                }
            }
        }
        return playerNr
    }

    fun getNextAvailTeamNr():Int {
        return if (getNextAvailTeamNr() < 3) {
            1
        } else {
            2
        }
    }

}