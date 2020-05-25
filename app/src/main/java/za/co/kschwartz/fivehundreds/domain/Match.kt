package za.co.kschwartz.fivehundreds.domain

class Match {

    var teams = mutableMapOf<Int,Team>();

    init {
        teams.clear();
        teams.put(1,Team(1))
        teams.put(2,Team(2))
    }

    fun joinPlayer(player: Player, teamNr: Int): Boolean {
        val team: Team? = teams.get(teamNr)
        if (team != null) {
            return team.addPlayer(player)
        }
        return false
    }

}