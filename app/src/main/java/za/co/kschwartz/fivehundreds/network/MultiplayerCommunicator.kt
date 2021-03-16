package za.co.kschwartz.fivehundreds.network

import za.co.kschwartz.fivehundreds.domain.*

interface MultiplayerCommunicator {

    val responseReceiver:ResponseReceiver

    fun createMatch(): Match

    fun joinMatch(matchId: String, playerName: String)

    fun leaveMatch(match: Match, player: Player)

    fun disconnect()

    fun switchPlayerSlot(newSlotNr: Int, player: Player)

    fun startMatch()

    fun startNewRound()

    fun startRound(round: Round)

    fun startNextPack(round: Round)

    fun placeBet(round: Round, bet: Bet)

    fun playCard(turn: Turn, card: Card)

}