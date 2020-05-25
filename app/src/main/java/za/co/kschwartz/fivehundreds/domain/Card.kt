package za.co.kschwartz.fivehundreds.domain

class Card(suit: Suit, value: Int) {
    val suit: Suit = suit
    val value: Int = value
    var isTrump: Boolean = false

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
}