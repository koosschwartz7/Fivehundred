package za.co.kschwartz.fivehundreds

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.containerPlayerHand
import kotlinx.android.synthetic.main.activity_game.txtGameID
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.player_card.view.*
import za.co.kschwartz.fivehundreds.domain.Match
import za.co.kschwartz.fivehundreds.domain.Player
import za.co.kschwartz.fivehundreds.domain.Round
import za.co.kschwartz.fivehundreds.domain.Suit
import za.co.kschwartz.fivehundreds.network.FirebaseCommunicator
import za.co.kschwartz.fivehundreds.network.MultiplayerCommunicator
import za.co.kschwartz.fivehundreds.network.ResponseReceiver

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameActivity : AppCompatActivity(), ResponseReceiver {

    private var gameId = ""
    private var teamNr = 99
    private var playerNr = 99

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var multiplayerCommunicator: MultiplayerCommunicator = FirebaseCommunicator(this)
    var match = Match()
    var uid = "undetermined"


    private var isFullscreen: Boolean = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        uid = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        gameId = intent.getStringExtra("GAMEID").toString()
        teamNr = intent.getIntExtra("TEAMNR", 99)
        playerNr = intent.getIntExtra("PLAYERNR", 99)

        if (gameId != null) {
            multiplayerCommunicator.joinMatch(gameId, getDisplayName())
            this.match.uniqueMatchCode = gameId
        }

        isFullscreen = true

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    private fun getDisplayName() = (user?.displayName ?: "??Broken User??")

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun joinMatchSuccess(match: Match) {
        //TODO: rejoin functionality
        this.match = match
        txtGameID.text = match.uniqueMatchCode
    }

    override fun joinMatchFailure(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun leaveMatchSuccess(match: Match) {
        //TODO("Not yet implemented")
    }

    override fun matchUpdated(match: Match) {
        this.match = match
        updateUI()
    }

    private fun updateUI() {
        val team1 = match.teams["Team 1"]
        val team2 = match.teams["Team 2"]
        val player1 = team1?.players!!["Player 1"]
        val player2 = team1?.players!!["Player 2"]
        val player3 = team2?.players!!["Player 1"]
        val player4 = team2?.players!!["Player 2"]

        var round: Round? = null
        try {
            round = match.rounds.last()
        } catch (e:NoSuchElementException) {
            println("Fresh game, starting new round.")
        }

        if (round!=null) {
            val player = determinePlayer(round)

            txtTeam1Score.text = "Team 1:"+round.getTotalScoreForTeam(1)
            txtTeam2Score.text = "Team 2:"+round.getTotalScoreForTeam(2)
            txtTeam1Packs.text = "Packs:"+round.getNrOfPacksTakenForTeam(1)
            txtTeam2Packs.text = "Packs:"+round.getNrOfPacksTakenForTeam(2)

            txtPlay1.text = round.players[0].name
            txtPlay2.text = round.players[1].name
            txtPlay3.text = round.players[2].name
            txtPlay4.text = round.players[3].name

            for (card in player.hand) {
                val cardLayout = this.layoutInflater.inflate(R.layout.player_card, null)
                cardLayout.imgCard.setImageResource(card.imgResId)
                containerPlayerHand.addView(cardLayout)
            }

            if (round.bet.trumpSuit == Suit.NULLSUIT) {
                txtBet.text = "Current Bet:"
            } else {
                txtBet.text = "Current Bet:"+round.bet.nrPacks+" of "+round.bet.getTrumpTitle()
            }

        } else if (uid == player1!!.uniqueID) {
            multiplayerCommunicator.startNewRound()
        }
    }

    fun determinePlayer(round:Round):Player {
            for (player in round.players) {
                if (player.uniqueID == uid) {
                    return player
                }
            }
        return Player()
    }
}