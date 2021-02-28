package za.co.kschwartz.fivehundreds

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_lobby.*
import za.co.kschwartz.fivehundreds.databinding.ActivityLobbyBinding
import za.co.kschwartz.fivehundreds.domain.Match
import za.co.kschwartz.fivehundreds.domain.MatchState
import za.co.kschwartz.fivehundreds.domain.Player
import za.co.kschwartz.fivehundreds.network.FirebaseCommunicator
import za.co.kschwartz.fivehundreds.network.MultiplayerCommunicator
import za.co.kschwartz.fivehundreds.network.ResponseReceiver

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LobbyActivity : AppCompatActivity(), ResponseReceiver {
    private val hideHandler = Handler()

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var multiplayerCommunicator: MultiplayerCommunicator = FirebaseCommunicator(this)
    var match = Match()
    var playerNr = 0
    var teamNr = 0
    var uid = "undetermined"

    private lateinit var binding: ActivityLobbyBinding

    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_lobby)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        uid = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        val gameId = intent.getStringExtra("GAMEID")
        if (gameId != null) {
            multiplayerCommunicator.joinMatch(gameId, getDisplayName())
            this.match.uniqueMatchCode = gameId
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lobby)
    }

    private fun getDisplayName() = (user?.displayName ?: "??Broken User??")

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

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
        this.match = match
        txtGameID.text = "Game ID: "+match.uniqueMatchCode
        if (playerAlreadyJoined()) {
            determinePlayerValues()
            if (match.status == MatchState.IN_PROGRESS) {
                startGameActivityFor(match)
            }
        } else {
            playerNr = match.getNextAvailablePlayerNr()
            teamNr = match.getNextAvailTeamNr()
            val player = Player(getDisplayName(), teamNr, playerNr, uid)
            multiplayerCommunicator.switchPlayerSlot(playerNr, player)
        }

    }

    override fun joinMatchFailure(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun leaveMatchSuccess(match: Match) {
        Toast.makeText(applicationContext, "Disconnected from match", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun matchUpdated(match: Match) {
        if (match.status == MatchState.IN_PROGRESS) {
            startGameActivityFor(match)
        } else {
            updateLobbyUI(match)
        }
    }

    private fun startGameActivityFor(match: Match) {
        multiplayerCommunicator.disconnect()
        Toast.makeText(applicationContext, "Game is starting!",Toast.LENGTH_SHORT).show()
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAMEID", match.uniqueMatchCode)
            putExtra("TEAMNR", teamNr)
            putExtra("PLAYERNR", playerNr)
        }
        startActivity(intent)
        //finish()
    }

    private fun updateLobbyUI(match: Match) {
        var waitingForNr = 4;
        val player1 = match.teams["Team 1"]?.players?.get("Player 1")
        val player2 = match.teams["Team 1"]?.players?.get("Player 2")
        val player3 = match.teams["Team 2"]?.players?.get("Player 1")
        val player4 = match.teams["Team 2"]?.players?.get("Player 2")

        if (player1 != null) {
            txtPlayer1.text = player1.name
            waitingForNr--
        } else {
            txtPlayer1.text = "1. (empty)"
        }

        if (player2 != null) {
            txtPlayer2.text = player2.name
            waitingForNr--
        } else {
            txtPlayer2.text = "2. (empty)"
        }

        if (player3 != null) {
            txtPlayer3.text = player3.name
            waitingForNr--
        } else {
            txtPlayer3.text = "3. (empty)"
        }

        if (player4 != null) {
            txtPlayer4.text = player4.name
            waitingForNr--
        } else {
            txtPlayer4.text = "4. (empty)"
        }

        btnStartGame.isEnabled = player1 != null && player2 != null && player3 != null && player4 != null
        if (waitingForNr > 0) {
            txtWaitingForPlayers.text = "Waiting for $waitingForNr more players"
        } else {
            txtWaitingForPlayers.text = "Ready to start the match!"
        }
    }

    fun btnStartGameClicked(view: View) {
        multiplayerCommunicator.startMatch()
    }

    private fun playerAlreadyJoined():Boolean {
        for (team in match.teams.values) {
            for (player in team.players.values) {
                if (player.uniqueID == uid) {
                    return true
                }
            }
        }
        return false
    }

    private fun determinePlayerValues() {
        var teamNr = 1
        for (team in match.teams.values) {
            var playerNr = 1
            for (player in team.players.values) {
                if (player.uniqueID == uid) {
                    this.playerNr = playerNr
                    this.teamNr = teamNr
                    return
                }
                playerNr++
            }
            teamNr++
        }
    }
}