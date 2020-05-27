package za.co.kschwartz.fivehundreds.domain

data class Bet(val trumpSuit: Suit, val callingPlayer: Player, val nrPacks: Int = 6) {

}