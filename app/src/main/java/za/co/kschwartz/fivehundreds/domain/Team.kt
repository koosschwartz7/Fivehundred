package za.co.kschwartz.fivehundreds.domain

import za.co.kschwartz.fivehundreds.R

class Team(teamNr: Int = 1) {

    var players = mutableMapOf<String,Player>()
    var score: Int = 0
    var teamNr: Int = teamNr

    fun addPlayer(player: Player):Boolean {
        if (players.size < 2) {
            val playerTeamNr = players.size + 1
            players["Player $playerTeamNr"] = player
            return true
        }
        return false
    }

    fun getTeamColorID(): Int {
        if (teamNr == 2) {
            return R.color.teamTwoColor
        }
        return  R.color.teamOneColor
    }

}