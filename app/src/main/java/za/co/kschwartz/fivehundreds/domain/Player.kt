package za.co.kschwartz.fivehundreds.domain

class Player(name: String, team: Int, playerNr: Int) {
    val name: String = name
    var hand = mutableListOf<Card>()
    var team: Int = team
    var playerNr: Int = playerNr

    fun playCard(indexOfCard: Int): Card = hand.removeAt(indexOfCard)
    fun call(suit: Suit, nrOfPacks: Int): Trump = Trump(suit, this, nrOfPacks)
    fun pass() {
        //TODO: Count passes perhaps?
    }
}