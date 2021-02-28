package za.co.kschwartz.fivehundreds.domain

class Round(player1: Player = Player(), player2: Player = Player(), player3: Player = Player(), player4: Player = Player(), roundNr: Int = 0, var nextBettingPlayerIndex: Int = 0) {
    val players = arrayListOf<Player>(player1, player2, player3, player4)
    val roundNr: Int = roundNr
    var packs = arrayListOf<Pack>(
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4),
            Pack(player1, player2, player3, player4)
    )
    var bet:Bet = Bet(Suit.NULLSUIT, players[nextBettingPlayerIndex], 99)
    var initialBettingPlayerIndex:Int = nextBettingPlayerIndex;

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

    fun incrementNextBettingPlayerIndex():Int {
        nextBettingPlayerIndex++
        if (nextBettingPlayerIndex >= 4) {
            nextBettingPlayerIndex = 0
        }
        return  nextBettingPlayerIndex;
    }

    fun getNextBettingPlayer():Player {
        val nextBettingPlayer:Player = players[nextBettingPlayerIndex]
        incrementNextBettingPlayerIndex();
        return nextBettingPlayer
    }

    fun generateNextPackPlayOrder() {
        val nextPlayablePackIndex = getNextPlayablePackIndex()
        if (nextPlayablePackIndex > -1) {
            val nextPack = packs[nextPlayablePackIndex]
            var playerIndex = getNextPackFirstPlayerIndex()

            for (turn in nextPack.turns) {
                turn.player = players[playerIndex]
                playerIndex = incrementPlayerIndex(playerIndex)
            }

        }
    }

    private fun incrementPlayerIndex(playerIndex: Int):Int {
        if (playerIndex == 3) {
            return 0
        }
        return playerIndex + 1
    }

    fun getNextPlayablePackIndex():Int {
        for (i in packs.indices) {
            if (!packs[i].allTurnsPlayed()) {
                return i
            }
        }
        return -1
    }

    fun getNextPackFirstPlayerIndex():Int {
        val lastPlayedPackIndex = getNextPlayablePackIndex() -1
        if (lastPlayedPackIndex > -1) {
            return players.indexOf(packs[lastPlayedPackIndex].determineWinningTurn(bet.trumpSuit).player)
        }
        return players.indexOf(bet.callingPlayer);
    }

    fun getTotalScoreForTeam(teamNr: Int): Int {
        var total:Int = 0
        for (score in getScoreBreakdownForTeam(teamNr).values) {
            total += score
        }
        return total
    }

    fun getScoreBreakdownForTeam(teamNr: Int): Map<String, Int> {
        val packsTaken: Int = getNrOfPacksTakenForTeam(teamNr)
        var breakdown = mutableMapOf<String, Int>()
        val betScoreValue = bet.trumpSuit.trumpWeight + ((bet.nrPacks-6) * 50)

        if (packsTaken == 10) {
            breakdown["Slam!"] = 250
            return breakdown
        }

        if (bet.nrPacks > packsTaken && teamNr == bet.callingPlayer.team) {//Betting team, bet failed
            breakdown["Bet failed!"] = 0
        } else if ((10 - packsTaken) < bet.nrPacks  && teamNr != bet.callingPlayer.team) {//Opposite team's bet failed
            breakdown["Other team went under!"] = betScoreValue
            breakdown["Extra packs (x$packsTaken)"] = (packsTaken*10)
        } else if (bet.nrPacks <= packsTaken && teamNr == bet.callingPlayer.team) {//Betting team, bet success
            val bonusPacks = (packsTaken - bet.nrPacks)
            breakdown[bet.nrPacks.toString() + " of " + bet.getTrumpTitle()] = betScoreValue
            breakdown["Extra packs (x$bonusPacks)"] = (bonusPacks * 10)
        } else {//Opposite team's bet was success
            breakdown["Packs taken (x$packsTaken)"] = (packsTaken*10)
        }
        return breakdown
    }

}