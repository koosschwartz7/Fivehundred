package za.co.kschwartz.fivehundreds.domain

class Pack(player1: Player, player2: Player, player3: Player, player4: Player)  {
    var winningTeam: Int = 0;
    var turns = arrayOf<Turn>(Turn(player1), Turn(player2), Turn(player3), Turn(player4))

    fun determineWinningTurn(trumpSuit: Suit): Turn {
        val packSuit: Suit = turns[0].playedCard.suit
        var winningTurn: Turn = turns[0]
        for (turn in turns) {
            val turnCard: Card = turn.playedCard
            val turnSuit: Suit = turn.playedCard.suit
            if (turn.equals(winningTurn) || turnSuit == Suit.NULLSUIT || (turnSuit != trumpSuit && turnSuit != packSuit)) {
                continue
            }
            if (turnSuit == Suit.JOKER) {
                winningTurn = turn
                break
            }
            if (packSuit == turnSuit && turnCard.value > winningTurn.playedCard.value) {
                winningTurn = turn
                continue
            }
            if (turnSuit == trumpSuit && winningTurn.playedCard.suit != trumpSuit) {
                winningTurn = turn
                continue
            }

        }
        return winningTurn
    }
}