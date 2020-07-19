package za.co.kschwartz.fivehundreds.domain

data class Bet(val trumpSuit: Suit, val callingPlayer: Player, var nrPacks: Int = 6) {
    fun getTrumpTitle(): String {
        if (trumpSuit == Suit.JOKER) {
            return "No Trumps";
        }
        return trumpSuit.title
    }
}