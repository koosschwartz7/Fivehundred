package za.co.kschwartz.fivehundreds.domain

data class Bet(val trumpSuit: Suit, val callingPlayer: Player, var nrPacks: Int = 6) {

}