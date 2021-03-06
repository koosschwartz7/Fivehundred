package za.co.kschwartz.fivehundreds.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import za.co.kschwartz.fivehundreds.domain.*

class FirebaseCommunicator(override val responseReceiver: ResponseReceiver) : MultiplayerCommunicator {


    var match: Match = Match()
    var database: DatabaseReference = Firebase.database.reference
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var matchUpdateListener = getMatchUpdatedListener()
    val deck = FivehundredDeck()


    override fun createMatch(): Match {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        fbMatchNode.setValue(match, getCreateMatchCompList())
        return match
    }

    private fun getCreateMatchCompList():DatabaseReference.CompletionListener {
        return DatabaseReference.CompletionListener { error, ref ->
            if (error == null) {
                Log.println(Log.INFO, "createMatch","Match ["+ref.key+"] started successfully.")
                joinMatch(match.uniqueMatchCode, (user?.displayName ?: "??Broken User??"))
            } else {
                Log.println(Log.ERROR, "createMatch", "Error creating new match on Firebase: " + error?.message+ " : "+ref.key)
            }
        }
    }

    override fun joinMatch(matchId: String, playerName: String) {
        val fbMatchNode = database.child("matches").child(matchId)
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.value != null) {
                    match = dataSnapshot.getValue<Match>() as Match
                    fbMatchNode.addValueEventListener(matchUpdateListener)
                    responseReceiver.joinMatchSuccess(match)
                } else {
                    responseReceiver.joinMatchFailure("The given game ID does not exist.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("joinMatch", "loadPost:onCancelled", databaseError.toException())
                responseReceiver.joinMatchFailure("Could not join match due to database exception.")
            }
        }
        fbMatchNode.addListenerForSingleValueEvent(postListener)
    }

    override fun leaveMatch(match: Match, player: Player) {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        match.teams["Team " + player.team]?.players?.remove("Player "+player.playerNr)
        fbMatchNode.setValue(match)
        disconnect()
    }

    override fun disconnect() {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        fbMatchNode.removeEventListener(matchUpdateListener)
    }

    private fun getMatchUpdatedListener():ValueEventListener  {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.println(Log.INFO, "matchUpdated","Match ["+dataSnapshot.key+"] has been updated, handle new changes.")
                match = dataSnapshot.getValue<Match>()!!
                responseReceiver.matchUpdated(match)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.println(Log.ERROR, "matchUpdated", "Error getting match updates: " + databaseError?.message)
            }
        }
        return postListener
    }

    override fun switchPlayerSlot(newSlotNr: Int, player: Player) {
        val playerCurrentlyInSlot = match.teams["Team " + player.team]?.players?.get("Player $newSlotNr")
        if (playerCurrentlyInSlot == null) {
            match.teams["Team " + player.team]?.addPlayer(player)
            val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
            fbMatchNode.setValue(match)
        }
    }

    override fun startMatch() {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        match.status = MatchState.IN_PROGRESS
        fbMatchNode.setValue(match)
    }

    override fun startNewRound() {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        val round = match.playNewRound()
        deck.reset()
        round.dealHand(deck)
        fbMatchNode.setValue(match)
    }

    override fun startRound(round: Round) {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        round.generateNextPackPlayOrder()
        round.state = RoundState.PLAYING
        fbMatchNode.setValue(match)
    }

    override fun startNextPack(round: Round) {
        if (round.currentPackIndex < 9) {
            round.currentPackIndex++
            val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
            fbMatchNode.setValue(match)
        }
    }

    override fun placeBet(round: Round, bet: Bet) {
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        round.placeBet(bet)
        fbMatchNode.setValue(match)
    }

    override fun playCard(turn: Turn, card: Card) {
        turn.playedCard = card

        val round = match.rounds.last()
        val nextPlayablePackIndex = round.getNextPlayablePackIndex()
        if (nextPlayablePackIndex != -1) {
            val pack = round.packs[nextPlayablePackIndex]
            if (pack.turns[0].playedCard.suit == Suit.NULLSUIT) {
                round.generateNextPackPlayOrder()
            }
        } else {
            for (i in 1..2) {
                match.teams["Team $i"]!!.score += round.getTotalScoreForTeam(i)
            }
        }

        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        fbMatchNode.setValue(match)
    }

    override fun endMatch(match: Match) {
        match.status = MatchState.FINISHED
        val fbMatchNode = database.child("matches").child(match.uniqueMatchCode)
        fbMatchNode.setValue(match)
    }

}