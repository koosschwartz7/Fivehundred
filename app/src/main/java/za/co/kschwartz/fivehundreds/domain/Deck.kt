package za.co.kschwartz.fivehundreds.domain

abstract class Deck(size: Int = 0) {
    val size: Int = size
    val cards = mutableListOf<Card>()

    fun isEmpty():Boolean = cards.isEmpty()

    abstract fun drawRandomCard(): Card
    abstract fun reset()
    abstract fun returnCard(card: Card)
}