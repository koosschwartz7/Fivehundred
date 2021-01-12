package za.co.kschwartz.fivehundreds.network

import za.co.kschwartz.fivehundreds.domain.Bet
import za.co.kschwartz.fivehundreds.domain.Card
import za.co.kschwartz.fivehundreds.domain.Match
import za.co.kschwartz.fivehundreds.domain.Player

class FirebaseCommunicator: MultiplayerCommunicator {
    override fun createMatch(): Match {
        TODO("Not yet implemented")
    }

    override fun joinMatch(matchId: String, playerName: String) {
        TODO("Not yet implemented")
    }

    override fun switchPlayerSlot(newSlotNr: Int, player: Player) {
        TODO("Not yet implemented")
    }

    override fun startMatch() {
        TODO("Not yet implemented")
    }

    override fun placeBet(bet: Bet) {
        TODO("Not yet implemented")
    }

    override fun passBet() {
        TODO("Not yet implemented")
    }

    override fun playCard(card: Card) {
        TODO("Not yet implemented")
    }

}