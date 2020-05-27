package za.co.kschwartz.fivehundreds.domain

import kotlin.random.Random

class FivehundredDeck(size: Int) : Deck(size) {

    override fun drawRandomCard(): Card {
        val index = Random.nextInt(cards.size)
        val randomCard = cards.get(index)
        cards.removeAt(index)
        return randomCard
    }

    override fun reset() {
        cards.clear()
        addAllCardsForSuitToDeck(Suit.JOKER)
        addAllCardsForSuitToDeck(Suit.CLUB)
        addAllCardsForSuitToDeck(Suit.DIAMOND)
        addAllCardsForSuitToDeck(Suit.SPADE)
        addAllCardsForSuitToDeck(Suit.HEART)
    }

    override fun returnCard(card: Card) {
        cards.add(card);
    }

    private fun addAllCardsForSuitToDeck(suit: Suit) {
        if (suit.equals(Suit.JOKER)) {
            cards.add(Card(Suit.JOKER, 15))
        } else if (suit.equals(Suit.HEART) || suit.equals(Suit.DIAMOND)) {
            for (i in 4..14) {
                cards.add(Card(suit, i))
            }
        } else {
            for (i in 5..14) {
                cards.add(Card(suit, i))
            }
        }
    }

}