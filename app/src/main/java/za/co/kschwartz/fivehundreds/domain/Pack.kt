package za.co.kschwartz.fivehundreds.domain

/**
 * A pack is the collection of the 4 turns played by the 4 players, for the total of 4 cards
 */
class Pack(player1: Player = Player(), player2: Player = Player(), player3: Player = Player(), player4: Player = Player())  {
    var turns = arrayOf<Turn>(Turn(player1), Turn(player2), Turn(player3), Turn(player4))

    /**
     * Determines the winning turn in this pack
     * @param trumpSuit The Suit that's been called as trumps in this round.
     * @return The Turn that wins the round by means of playing the highest card
    */
    fun determineWinningTurn(trumpSuit: Suit): Turn {
        val packSuit: Suit = turns[0].playedCard.suit
        var winningTurn: Turn = turns[0]
        for (turn in turns) {
            val turnCard: Card = turn.playedCard
            val turnSuit: Suit = turn.playedCard.suit
            if (turnSuit == Suit.JOKER) {
                winningTurn = turn
                break
            }
            if (turn == winningTurn || turnSuit == Suit.NULLSUIT || (!turnCard.isTrump(trumpSuit) && turnSuit != packSuit)) {
                continue
            }
            if (turnCard.isTrump(trumpSuit) && !winningTurn.playedCard.isTrump(trumpSuit)) {
                winningTurn = turn
                continue
            }
            if (winningTurn.playedCard.isTrump(trumpSuit) && turnCard.isTrump(trumpSuit)) {
                if ((turnCard.value > winningTurn.playedCard.value || turnCard.value == 11) && winningTurn.playedCard.value != 11) {
                    winningTurn = turn
                    continue
                } else if (winningTurn.playedCard.value == 11 && turnCard.value == 11 && turnCard.suit == trumpSuit) {
                    winningTurn = turn
                    continue
                }
            }
            if (winningTurn.playedCard.suit != trumpSuit && packSuit == turnSuit && turnCard.value > winningTurn.playedCard.value) {
                winningTurn = turn
                continue
            }
        }
        return winningTurn
    }

    /**
     * Determines if all players have played a turn.
     * @return TRUE if all players have played their turn, else FALSE.
     */
    fun allTurnsPlayed(): Boolean {
        var allTurnsPlayed: Boolean = true
        for (t in turns) {
            if (t.playedCard.suit == Suit.NULLSUIT) {
                allTurnsPlayed = false
                break
            }
        }
        return allTurnsPlayed
    }

    fun getNextPlayableTurn(): Turn {
        for (t in turns) {
            if (t.playedCard.suit == Suit.NULLSUIT) {
                return t
            }
        }
        throw NoNextTurnException("All turns in this pack have been played.");
    }


}