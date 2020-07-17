package za.co.kschwartz.fivehundreds.domain

enum class Suit(val title: String, val trumpWeight: Int) {
    SPADE("Spades", 1), CLUB("Clubs", 2),
    DIAMOND("Diamonds", 3), HEART("Hearts", 4 ),
    JOKER("Jokers", 5), NULLSUIT("NoSuit", 0)
}