package za.co.kschwartz.fivehundreds.domain

class Player(name: String = "", team: Int = 1, playerNr: Int = 99, uniqueID: String = "") {
    val name: String = name
    var hand = arrayListOf<Card>()
    var team: Int = team
    var playerNr: Int = playerNr
    var uniqueID:String = uniqueID

    fun playCard(indexOfCard: Int): Card = hand.removeAt(indexOfCard)
    fun call(suit: Suit, nrOfPacks: Int): Bet = Bet(suit, this, nrOfPacks)
    fun pass() {
        //TODO: Count passes perhaps?
    }


}