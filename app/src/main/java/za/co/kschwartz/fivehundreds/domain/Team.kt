package za.co.kschwartz.fivehundreds.domain

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

}