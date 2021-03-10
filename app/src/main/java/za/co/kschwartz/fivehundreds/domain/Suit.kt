package za.co.kschwartz.fivehundreds.domain

enum class Suit(val title: String, val trumpWeight: Int) {
    SPADE("Spades", 40), CLUB("Clubs", 60),
    DIAMOND("Diamonds", 80), HEART("Hearts", 100),
    JOKER("Joker", 120), NULLSUIT("NoSuit", 0)
}