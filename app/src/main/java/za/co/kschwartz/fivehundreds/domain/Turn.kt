package za.co.kschwartz.fivehundreds.domain

class Turn(player: Player = Player()) {

    var player: Player = player
    var playedCard: Card = Card(Suit.NULLSUIT, 0)

}