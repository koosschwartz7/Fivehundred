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
import za.co.kschwartz.fivehundreds.domain.Bet
import za.co.kschwartz.fivehundreds.domain.Card
import za.co.kschwartz.fivehundreds.domain.Match
import za.co.kschwartz.fivehundreds.domain.Player

class FirebaseCommunicator(override val responseReceiver: ResponseReceiver) : MultiplayerCommunicator {


    var match: Match = Match()
    var database: DatabaseReference = Firebase.database.reference
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser


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
                if (dataSnapshot.exists()) {
                    match = dataSnapshot.getValue<Match>() as Match
                    responseReceiver.joinMatchSuccess()
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