package za.co.kschwartz.fivehundreds.domain

class Round(player1: Player = Player(), player2: Player = Player(), player3: Player = Player(), player4: Player = Player(), roundNr: Int = 0, var nextBettingPlayerIndex: Int = 0) {
    val players = arrayListOf<Player>(player1, player2, player3, player4)
    val roundNr: Int = roundNr
    var currentPackIndex = 0
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
    var betHistory = arrayListOf<Bet>()
    var initialBettingPlayerIndex:Int = nextBettingPlayerIndex
    var state = RoundState.BETTING
    var deck: FivehundredDeck? = null

    fun dealHand(deck: FivehundredDeck) {
        this.deck = deck
        for (p in players) {
            p.hand.clear()
            for (i in 0..9) {
                p.hand.add(i, deck.drawRandomCard())
            }
            p.sortPlayerHand(bet.trumpSuit)
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
        if (newBet.trumpSuit == Suit.NULLSUIT) {
            betHistory.add(newBet)
            incrementNextBettingPlayerIndex()
            return true
        } else if (mayPlaceBet(newBet)) {
            bet = newBet
            betHistory.add(bet)
            for (p in players) {
                p.sortPlayerHand(bet.trumpSuit)
            }
            incrementNextBettingPlayerIndex()
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

        //Betting a NULLSUIT bet is used as passing (not betting)
        if (suit == Suit.NULLSUIT) {
            if (callingPlayer.uniqueID == players[0].uniqueID && bet.trumpSuit == Suit.NULLSUIT && betHistory.size > 0) {
                if (betHistory[0].trumpSuit == Suit.NULLSUIT && betHistory[0].callingPlayer.uniqueID == callingPlayer.uniqueID) {
                    throw SuitBetNotAllowedException("You are only allowed to pass once.")
                }
            }
            minimumBet.nrPacks = 5
        }

        if (bet.trumpSuit.equals(Suit.NULLSUIT)) {
            return minimumBet
        }

        if (suit.trumpWeight <= bet.trumpSuit.trumpWeight && bet.nrPacks == 10) {
            throw SuitBetNotAllowedException("Suit "+suit.title+" has no possible bet that's greater than 10 of "+bet.trumpSuit.title)
        }

        if (suit.trumpWeight <= bet.trumpSuit.trumpWeight) {
           minimumBet.nrPacks = bet.nrPacks+1
        } else {
            minimumBet.nrPacks = bet.nrPacks
        }

        return minimumBet
    }

    fun allOtherPlayersHavePassed():Boolean {
        if (bet.trumpSuit != Suit.NULLSUIT) {
            var passes = -1
            for (hBet in betHistory) {
                if (hBet.callingPlayer.uniqueID == bet.callingPlayer.uniqueID && hBet.nrPacks == bet.nrPacks && hBet.trumpSuit == bet.trumpSuit) {
                    passes = 0
                }
                if (hBet.trumpSuit == Suit.NULLSUIT) {
                    passes ++
                    if (passes >=3) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun incrementNextBettingPlayerIndex():Int {
        nextBettingPlayerIndex++
        if (nextBettingPlayerIndex >= 4) {
            nextBettingPlayerIndex = 0
        }
        return  nextBettingPlayerIndex;
    }

    fun getNextBettingPlayer():Player {
        return players[nextBettingPlayerIndex]
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

    fun getCurrentPack():Pack {
        return packs[currentPackIndex]
    }

    fun getNextPackFirstPlayerIndex():Int {
        val lastPlayedPackIndex = getNextPlayablePackIndex() -1
        var playerUid = bet.callingPlayer.uniqueID
        if (lastPlayedPackIndex > -1) {
            playerUid = packs[lastPlayedPackIndex].determineWinningTurn(bet.trumpSuit).player.uniqueID
        }
        for (index in players.indices) {
            if (players[index].uniqueID == playerUid) {
                return index
            }
        }
        return -1
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

        if (bet.trumpSuit == Suit.NULLSUIT) {
            breakdown["No Bet"] = 0
            return breakdown
        }

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