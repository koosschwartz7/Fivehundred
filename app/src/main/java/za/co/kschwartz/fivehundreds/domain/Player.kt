package za.co.kschwartz.fivehundreds.domain

class Player(name: String = "", team: Int = 1, playerNr: Int = 99, uniqueID: String = "") {
    val name: String = name
    var hand = arrayListOf<Card>()
    var team: Int = team
    var playerNr: Int = playerNr
    var uniqueID:String = uniqueID

    fun playCard(indexOfCard: Int): Card = hand.removeAt(indexOfCard)
    fun call(suit: Suit, nrOfPacks: Int): Bet = Bet(suit, this, nrOfPacks)
    fun pass() {
        //TODO: Count passes perhaps?
    }

    fun sortPlayerHand(trumpSuit: Suit) {
        var handBySuitList: Map<Suit,ArrayList<Card>> = mapOf(
                Suit.SPADE to ArrayList(hand.filter { it.suit == Suit.SPADE }.sortedWith(compareBy(Card::value))),
                Suit.CLUB to ArrayList(hand.filter { it.suit == Suit.CLUB }.sortedWith(compareBy(Card::value))),
                Suit.DIAMOND to ArrayList(hand.filter { it.suit == Suit.DIAMOND }.sortedWith(compareBy(Card::value))),
                Suit.HEART to ArrayList(hand.filter { it.suit == Suit.HEART }.sortedWith(compareBy(Card::value))),
                Suit.JOKER to ArrayList(hand.filter { it.suit == Suit.JOKER })
        )


        if (trumpSuit != Suit.NULLSUIT) {
            var trumpSuitList = arrayListOf<Card>()
            if (handBySuitList[trumpSuit] != null) {
                trumpSuitList = handBySuitList[trumpSuit]!!
            }
            for (suit in Suit.values()) {
                if (suit == Suit.NULLSUIT || suit == trumpSuit) {
                    continue
                }

                val suitList = handBySuitList[suit]
                if (suitList != null) {
                    for (card in suitList) {
                        if (card.isTrump(trumpSuit)) {
                            trumpSuitList!!.add(card)
                        }
                    }
                }
            }

            //Avoid concurrent modification exception
            for (trumpCard in trumpSuitList) {
                for (suit in Suit.values()) {
                    if (suit == Suit.NULLSUIT || suit == trumpSuit) {
                        continue
                    }
                    val suitList = handBySuitList[suit]
                    if (suitList != null && suitList.contains(trumpCard)) {
                        suitList.remove(trumpCard)
                    }
                }
            }
        }

        hand = handBySuitList[Suit.SPADE]!!
        hand.addAll(handBySuitList[Suit.CLUB]!!)
        hand.addAll(handBySuitList[Suit.DIAMOND]!!)
        hand.addAll(handBySuitList[Suit.HEART]!!)
        hand.addAll(handBySuitList[Suit.JOKER]!!)

    }


}