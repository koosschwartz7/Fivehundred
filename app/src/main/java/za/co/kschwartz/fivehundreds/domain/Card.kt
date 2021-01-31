package za.co.kschwartz.fivehundreds.domain

class Card(suit: Suit = Suit.NULLSUIT, value: Int = 0) {
    val suit: Suit = suit
    val value: Int = value

    fun getDisplayName(): String {
        if (value == 15) {
            return getValueDisplayName()
        } else {
            return getValueDisplayName() + " of " + suit.title
        }
    }

    private fun getValueDisplayName(): String {
        when (value) {
            11 -> return "Jack"
            12 -> return "Queen"
            13 -> return "King"
            14 -> return "Ace"
            15 -> return "Joker"
            else -> {
                return value.toString()
            }
        }
    }

    fun isTrump(trumpSuit: Suit):Boolean {
        if (trumpSuit.equals(Suit.SPADE)) {
            return suit.equals(Suit.SPADE) || (suit.equals(Suit.CLUB) && value == 11)
        } else if (trumpSuit.equals(Suit.CLUB)) {
            return suit.equals(Suit.CLUB) || (suit.equals(Suit.SPADE) && value == 11)
        } else if (trumpSuit.equals(Suit.HEART)) {
            return suit.equals(Suit.HEART) || (suit.equals(Suit.DIAMOND) && value == 11)
        } else if (trumpSuit.equals(Suit.DIAMOND)) {
            return suit.equals(Suit.DIAMOND) || (suit.equals(Suit.HEART) && value == 11)
        } else return suit.equals(Suit.JOKER)
    }
}