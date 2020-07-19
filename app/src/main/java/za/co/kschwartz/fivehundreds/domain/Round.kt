package za.co.kschwartz.fivehundreds.domain

class Round(player1: Player, player2: Player, player3: Player, player4: Player, roundNr: Int, startingBettingPlayerIndex: Int) {
    val players = arrayOf<Player>(player1, player2, player3, player4)
    val roundNr: Int = roundNr
    var packs = Array<Pack>(10){Pack(player1, player2, player3, player4)}
    var bet:Bet = Bet(Suit.NULLSUIT, players[startingBettingPlayerIndex], 99)
    var nextPlayer:Int = 0

    fun dealHand(deck: Deck) {
        for (p in players) {
            p.hand.clear()
            for (i in 0..9) {
                p.hand.add(i, deck.drawRandomCard())
            }
        }
    }

    fun getNrOfPacksTakenForTeam(teamNr: Int):Int {
        var packsTaken: Int = 0
        for (p in packs) {
            if (p.allTurnsPlayed() && p.determineWinningTurn(bet.trumpSuit).player.team == teamNr) {
                packsTaken++
            }
        }
        return packsTaken
    }

    fun placeBet(newBet: Bet): Boolean {
        if (mayPlaceBet(newBet)) {
            bet = newBet
            return true
        }
        return false
    }

    //TODO: Test
    fun mayPlaceBet(possibleBet: Bet):Boolean {
        return possibleBet.nrPacks >= getMinimumAllowedBetForSuit(possibleBet.trumpSuit, possibleBet.callingPlayer).nrPacks
                && possibleBet.nrPacks <= 10
    }

    fun getMinimumAllowedBetForSuit(suit: Suit, callingPlayer: Player): Bet {
        val minimumBet: Bet = Bet(suit, callingPlayer)

        if (suit.equals(Suit.NULLSUIT)) {
            throw SuitBetNotAllowedException(Suit.NULLSUIT.title + " is not allowed as a bet!")
        }

        if (bet.trumpSuit.equals(Suit.NULLSUIT)) {
            return minimumBet
        }

        if (suit.trumpWeight <= bet.trumpSuit.trumpWeight && bet.nrPacks == 10) {
            throw SuitBetNotAllowedException("Suit "+suit.title+" has no possible bet that's greater than 10 of "+bet.trumpSuit.title)
        }

        if (suit.trumpWeight <= bet.trumpSuit.trumpWeight) {
           minimumBet.nrPacks = bet.nrPacks+1
        }

        return minimumBet
    }

}