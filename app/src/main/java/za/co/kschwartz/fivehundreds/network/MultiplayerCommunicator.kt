package za.co.kschwartz.fivehundreds.network

import za.co.kschwartz.fivehundreds.domain.Bet
import za.co.kschwartz.fivehundreds.domain.Card
import za.co.kschwartz.fivehundreds.domain.Match
import za.co.kschwartz.fivehundreds.domain.Player

interface MultiplayerCommunicator {

    val responseReceiver:ResponseReceiver

    fun createMatch(): Match

    fun joinMatch(matchId: String, playerName: String)

    fun leaveMatch(match: Match, player: Player)

    fun switchPlayerSlot(newSlotNr: Int, player: Player)

    fun startMatch()

    fun placeBet(bet: Bet)

    fun passBet()

    fun playCard(card: Card)

}