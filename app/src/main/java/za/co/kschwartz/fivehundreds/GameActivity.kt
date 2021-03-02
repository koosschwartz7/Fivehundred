package za.co.kschwartz.fivehundreds

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.containerPlayerHand
import kotlinx.android.synthetic.main.activity_game.txtGameID
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.bet_history_instance.view.*
import kotlinx.android.synthetic.main.player_card.view.*
import za.co.kschwartz.fivehundreds.domain.*
import za.co.kschwartz.fivehundreds.network.FirebaseCommunicator
import za.co.kschwartz.fivehundreds.network.MultiplayerCommunicator
import za.co.kschwartz.fivehundreds.network.ResponseReceiver
import java.lang.NumberFormatException

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
    var player = Player()
    var currentRound = Round()

    private var isFullscreen: Boolean = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        uid = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        gameId = intent.getStringExtra("GAMEID").toString()

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
        this.match = match
        player = determinePlayer(match)
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

        var round: Round? = null
        try {
            round = match.rounds.last()
        } catch (e:NoSuchElementException) {
            println("Fresh game, starting new round.")
        }

        txtTeam1Score.text = "Team 1:" + team1.score.toString()
        txtTeam2Score.text = "Team 2:" + team2?.score.toString()

        if (round!=null) {
            currentRound = round
            player = determinePlayer(round)

            for (card in player.hand) {
                val cardLayout = this.layoutInflater.inflate(R.layout.player_card, null)
                cardLayout.imgCard.setImageResource(card.imgResId)
                containerPlayerHand.addView(cardLayout)
            }

            if (round.state == RoundState.BETTING) {
                llBettingScreen.visibility = View.VISIBLE
                llGameScreen.visibility = View.GONE
                setBettingScreenValues(round)
            } else if (round.state == RoundState.PLAYING) {
                llBettingScreen.visibility = View.GONE
                llGameScreen.visibility = View.VISIBLE
                setGameScreenValues(round)
            } else if (round.state == RoundState.FINISHED) {
                llBettingScreen.visibility = View.GONE
                llGameScreen.visibility = View.GONE
                if (team1.score < 500 && team2?.score!! < 500) {
                    if (uid == player1!!.uniqueID) {
                        multiplayerCommunicator.startNewRound()
                    }
                } else {
                    //TODO: Match finished screen
                }
            }

        } else if (uid == player1!!.uniqueID) {
            multiplayerCommunicator.startNewRound()
        }
    }

    private fun setBettingScreenValues(round: Round) {
        val nextBettingPlayer = round.getNextBettingPlayer()

        for (i in 1 until tblBetHistory.childCount) {
            tblBetHistory.removeViewAt(i)
        }
        for (bet in round.betHistory) {
            addBetHistoryRow(bet)
        }

        txtCurrentBet.text = round.bet.callingPlayer.name + "(T"+round.bet.callingPlayer.team+") - "+ round.bet.getBetDescription()

        if (nextBettingPlayer.uniqueID == uid) {
            txtBetTurn.text = "It's YOUR TURN to bet:"
            llPlaceBetScreen.visibility = View.VISIBLE
            var minimumAllowedBet = round.getMinimumAllowedBetForSuit(Suit.SPADE, nextBettingPlayer)
            btnSpades.isChecked = true
            edtBetNrPacks.setText(minimumAllowedBet.nrPacks.toString())
        } else {
            txtBetTurn.text = "It's "+nextBettingPlayer.name+"'s turn to bet..."
            llPlaceBetScreen.visibility = View.GONE
        }
    }

    private fun addBetHistoryRow(bet: Bet) {
        val betHistoryInst = this.layoutInflater.inflate(R.layout.bet_history_instance, null)
        val betDesc = bet.callingPlayer.name + " calls " + bet.nrPacks + " of " + bet.getTrumpTitle()
        if (bet.callingPlayer.team == 1) {
            betHistoryInst.txtTeam1Bet.text = betDesc
        } else {
            betHistoryInst.txtTeam1Bet.text = betDesc
        }
        tblBetHistory.addView(betHistoryInst)
    }

    private fun setGameScreenValues(round: Round) {
        txtTeam1Packs.text = "Packs:" + round.getNrOfPacksTakenForTeam(1)
        txtTeam2Packs.text = "Packs:" + round.getNrOfPacksTakenForTeam(2)

        txtPlay1.text = round.players[0].name
        txtPlay2.text = round.players[1].name
        txtPlay3.text = round.players[2].name
        txtPlay4.text = round.players[3].name

        if (round.bet.trumpSuit == Suit.NULLSUIT) {
            txtBet.text = "Current Bet:"
        } else {
            txtBet.text = "Current Bet:" + round.bet.nrPacks + " of " + round.bet.getTrumpTitle()
        }
    }

    private fun determinePlayer(round:Round):Player {
            for (player in round.players) {
                if (player.uniqueID == uid) {
                    return player
                }
            }
        return Player()
    }

    private fun determinePlayer(match:Match):Player {
        for (team in match.teams.values) {
            for (player in team.players.values) {
                if (player.uniqueID == uid) {
                    return player
                }
            }
        }
        return Player()
    }

    fun btnPassClicked(view: View) {
        val passBet = Bet(Suit.NULLSUIT, player, 6)
        currentRound.placeBet(getBetFromPlayerInput())
    }

    fun btnPlaceBetClicked(view: View) {
        try {
            currentRound.placeBet(getBetFromPlayerInput())
        } catch (e: SuitBetNotAllowedException) {
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            builder.setMessage(e.message)
                .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                .show()
        }
    }

    private fun getBetFromPlayerInput(): Bet {
        var suit = Suit.NULLSUIT
        when {
            btnSpades.isChecked -> {
                suit = Suit.SPADE
            }
            btnClubs.isChecked -> {
                suit = Suit.CLUB
            }
            btnDiamonds.isChecked -> {
                suit = Suit.DIAMOND
            }
            btnNoTrumps.isChecked -> {
                suit = Suit.JOKER
            }
        }
        var nrPacks = 6
        try {
            nrPacks = Integer.parseInt(edtBetNrPacks.text.toString())
        } catch (e: NumberFormatException) {
            Log.println(Log.WARN, "PlaceBet", "Unable to parse input for nrOfPacks ["+edtBetNrPacks.text.toString()+"] - defaulting to 6")
        }
        return Bet(suit, player, nrPacks)
    }
}