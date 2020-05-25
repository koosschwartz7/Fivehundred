package za.co.kschwartz.fivehundreds.domain

class Turn(player: Player) {

    val player: Player = player
    var playedCard: Card = Card(Suit.NULLSUIT, 0)

}