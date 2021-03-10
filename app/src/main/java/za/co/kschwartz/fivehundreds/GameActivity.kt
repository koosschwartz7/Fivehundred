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
    var currentPack = Pack()
    var currentTurn = Turn()

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
            currentPack = round.packs[round.getNextPlayablePackIndex()]
            currentTurn = currentPack.getNextPlayableTurn()

            populatePlayerHandContainer()

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

    private fun populatePlayerHandContainer() {
        containerPlayerHand.removeAllViews()
        for (card in player.hand) {
            val cardLayout = this.layoutInflater.inflate(R.layout.player_card, null)
            cardLayout.imgCard.setImageResource(card.imgResId)
            cardLayout.setOnClickListener(View.OnClickListener {
                if (currentRound.state == RoundState.BETTING && currentRound.allOtherPlayersHavePassed() && currentRound.bet.callingPlayer.uniqueID == uid) {
                    currentRound.deck!!.cards.add(card)
                    player.hand.remove(card)
                    checkRoundStartable()
                } else if (currentRound.state == RoundState.PLAYING && currentTurn.player.uniqueID == uid) {
                    if (currentPack.mayPlayCard(player, card, currentRound.bet.trumpSuit)) {
                        player.hand.remove(card)
                        multiplayerCommunicator.playCard(currentTurn, card)
                    } else {
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setMessage("You cannot play this card.")
                            .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                            })
                            .show()
                    }
                }
                populatePlayerHandContainer()
                populateKittyContainer(currentRound)
            })
            containerPlayerHand.addView(cardLayout)
        }
    }

    private fun setBettingScreenValues(round: Round) {

        for (i in 1 until tblBetHistory.childCount) {
            tblBetHistory.removeViewAt(1)
        }
        for (bet in round.betHistory) {
            addBetHistoryRow(bet)
        }

        var betText = "Current bet: "+round.bet.callingPlayer.name + "(T"+round.bet.callingPlayer.team+") - "+ round.bet.getBetDescription()
        if (round.bet.trumpSuit == Suit.NULLSUIT) {
            betText = "Current bet: -"
        }
        txtCurrentBet.text = betText

        if (round.allOtherPlayersHavePassed()) {
            llPlaceBetScreen.visibility = View.GONE
            txtBetTurn.text = "Waiting for "+round.bet.callingPlayer.name+" to start the round"
            if (round.bet.callingPlayer.uniqueID == uid) {
                llKittyScreen.visibility = View.VISIBLE
                populateKittyContainer(round)
                checkRoundStartable()
            }

        } else {
            llKittyScreen.visibility = View.GONE
            val nextBettingPlayer = round.getNextBettingPlayer()
            if (nextBettingPlayer.uniqueID == uid) {
                txtBetTurn.text = "It's YOUR TURN to bet:"
                llPlaceBetScreen.visibility = View.VISIBLE
                var minimumAllowedBet =
                    round.getMinimumAllowedBetForSuit(Suit.SPADE, nextBettingPlayer)
                btnSpades.isChecked = true
                edtBetNrPacks.setText(minimumAllowedBet.nrPacks.toString())
            } else {
                txtBetTurn.text = "It's " + nextBettingPlayer.name + "'s turn to bet..."
                llPlaceBetScreen.visibility = View.GONE
            }
        }
    }

    private fun populateKittyContainer(round: Round) {
        llKittyContainer.removeAllViews()
        for (card in round.deck!!.cards) {
            val cardLayout = this.layoutInflater.inflate(R.layout.player_card, null)
            cardLayout.imgCard.setImageResource(card.imgResId)
            cardLayout.setOnClickListener(View.OnClickListener {
                player.hand.add(card)
                round.deck!!.cards.remove(card)
                checkRoundStartable()
                populateKittyContainer(round)
                populatePlayerHandContainer()
            })
            llKittyContainer.addView(cardLayout)
        }
    }

    private fun checkRoundStartable() {
        if (player.hand.size != 10) {
            txtStartMatchWarning.visibility = View.VISIBLE
            btnStartRound.isEnabled = false
        } else {
            txtStartMatchWarning.visibility = View.INVISIBLE
            btnStartRound.isEnabled = true
        }
    }

    private fun addBetHistoryRow(bet: Bet) {
        val betHistoryInst = this.layoutInflater.inflate(R.layout.bet_history_instance, null)
        var betDesc = bet.callingPlayer.name + " called " + bet.nrPacks + " of " + bet.getTrumpTitle()
        if (bet.trumpSuit == Suit.NULLSUIT) {
            betDesc = bet.callingPlayer.name + " passed."
        }

        betHistoryInst.txtTeam1Bet.text = betDesc
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
        try {
            multiplayerCommunicator.placeBet(currentRound, passBet)
        } catch (e: SuitBetNotAllowedException) {
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            builder.setMessage(e.message)
                .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                .show()
        }
    }

    fun btnPlaceBetClicked(view: View) {
        try {
            multiplayerCommunicator.placeBet(currentRound, getBetFromPlayerInput())
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
            btnHearts.isChecked -> {
                suit = Suit.HEART
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

    fun btnSuitSelectClicked(view: View) {
        estimateWinningScoreForUserInput()
    }

    private fun estimateWinningScoreForUserInput() {
        val bet = getBetFromPlayerInput()
        txtScorePrediction.text = (bet.trumpSuit.trumpWeight + ((bet.nrPacks -6)*50)).toString()
    }

    fun btnStartRoundClicked(view: View) {
        multiplayerCommunicator.startRound(currentRound)
    }
}