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


}