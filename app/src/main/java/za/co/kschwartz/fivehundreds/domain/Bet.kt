package za.co.kschwartz.fivehundreds.domain

data class Bet(val trumpSuit: Suit = Suit.NULLSUIT, val callingPlayer: Player = Player(), var nrPacks: Int = 6) {
    fun getTrumpTitle(): String {
        if (trumpSuit == Suit.JOKER) {
            return "No Trumps";
        }
        return trumpSuit.title
    }

    fun getBetDescription():String {
        return "$nrPacks of "+getTrumpTitle()
    }
}