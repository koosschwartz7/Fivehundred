package za.co.kschwartz.fivehundreds.domain

class Team(teamNr: Int = 1) {

    var players = mutableMapOf<Int,Player>()
    var score: Int = 0
    var teamNr: Int = teamNr

    fun addPlayer(player: Player):Boolean {
        if (players.size < 2) {
            players.put(players.size+1, player)
            return true
        }
        return false
    }

}