package za.co.kschwartz.fivehundreds

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.bet_history_instance.view.*
import kotlinx.android.synthetic.main.player_card.view.*
import kotlinx.android.synthetic.main.round_breakdown.*
import kotlinx.android.synthetic.main.round_breakdown.view.*
import za.co.kschwartz.fivehundreds.domain.*
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
    var player = Player()
    var currentRound = Round()
    var currentPack = Pack()
    var currentTurn = Turn()
    var currentPackIndex = 0

    val roundBreakdownLayout = this.layoutInflater.inflate(R.layout.round_breakdown, null)
    val breakDownPopup = PopupWindow(roundBreakdownLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

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
        } catch (e: NoSuchElementException) {
            println("Fresh game, starting new round.")
        }

        txtTeam1Score.text = "Team 1:" + team1.score.toString()
        txtTeam2Score.text = "Team 2:" + team2?.score.toString()

        if (round!=null) {
            currentRound = round
            player = determinePlayer(round)
            val nextPlayablePackIndex = round.getNextPlayablePackIndex()
            if (nextPlayablePackIndex == -1) {
                showRoundBreakDown()
            } else if (nextPlayablePackIndex != round.currentPackIndex) {
                delayStartNextPack()
            } else if (breakDownPopup.isShowing) {
                breakDownPopup.dismiss()
            }
            //currentPack = round.packs[round.getNextPlayablePackIndex()]
            currentPack = round.getCurrentPack()
            if (currentPack.allTurnsPlayed()) {
                val winningTurn = currentPack.determineWinningTurn(round.bet.trumpSuit)
                Toast.makeText(this,
                        winningTurn.player.name + " (Team " + winningTurn.player.team + ") wins pack with " + winningTurn.playedCard.getDisplayName(),
                        Toast.LENGTH_LONG)
                        .show()
            } else {
                currentTurn = currentPack.nextPlayableTurn()
            }

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
                    if (isPlayerOne()) {
                        multiplayerCommunicator.startNewRound()
                    }
                } else {
                    //TODO: Match finished screen
                }
            }

        } else if (isPlayerOne()) {
            multiplayerCommunicator.startNewRound()
        }
    }

    private fun showRoundBreakDown() {

        roundBreakdownLayout.txtPacksTakenTeam1.text = "Team 1: "+currentRound.getNrOfPacksTakenForTeam(1).toString()
        roundBreakdownLayout.txtPacksTakenTeam2.text = "Team 2: "+currentRound.getNrOfPacksTakenForTeam(2).toString()
        var scoreBreakdownText = "Team 1:\n"
        val scoreBreakdownTeam1 = currentRound.getScoreBreakdownForTeam(1)
        val scoreBreakdownTeam2 = currentRound.getScoreBreakdownForTeam(2)
        for (item in scoreBreakdownTeam1.keys) {
            scoreBreakdownText+= item+": "+scoreBreakdownTeam1[item]+"\n"
        }
        scoreBreakdownText += "-------\nTeam 2:\n"
        for (item in scoreBreakdownTeam2.keys) {
            scoreBreakdownText+= item+": "+scoreBreakdownTeam2[item]+"\n"
        }
        roundBreakdownLayout.txtScoreBreakdown.text = scoreBreakdownText

        roundBreakdownLayout.txtMatchTotalTeam1.text = "Team 1: "+match.teams["Team 1"]!!.score
        roundBreakdownLayout.txtMatchTotalTeam2.text = "Team 2: "+match.teams["Team 2"]!!.score

        roundBreakdownLayout.btnStartNextRound.visibility = View.GONE
        roundBreakdownLayout.btnEndMatch.visibility = View.GONE
        roundBreakdownLayout.txtWinningTeam.visibility = View.GONE
        if (match.teams["Team 1"]!!.score >= 500) {
            roundBreakdownLayout.btnEndMatch.visibility = View.VISIBLE
            roundBreakdownLayout.txtWinningTeam.visibility = View.VISIBLE
            roundBreakdownLayout.txtWinningTeam.text = "WINNING TEAM: TEAM 1!"
        } else if (match.teams["Team 2"]!!.score >= 500) {
            roundBreakdownLayout.btnEndMatch.visibility = View.VISIBLE
            roundBreakdownLayout.txtWinningTeam.visibility = View.VISIBLE
            roundBreakdownLayout.txtWinningTeam.text = "WINNING TEAM: TEAM 2!"
        } else {
            if (isPlayerOne()) {
                roundBreakdownLayout.btnStartNextRound.visibility = View.VISIBLE
                roundBreakdownLayout.btnStartNextRound.setOnClickListener(View.OnClickListener {
                    multiplayerCommunicator.startNewRound()
                    breakDownPopup.dismiss()
                })
            }
        }

        breakDownPopup.showAtLocation(llMainContainer, Gravity.CENTER, 0,0)
    }

    private fun delayStartNextPack() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isPlayerOne()) {
                multiplayerCommunicator.startNextPack(currentRound)
            }
        }, 4000)
    }

    private fun isPlayerOne() = uid == match.teams["Team 1"]!!.players!!["Player 1"]!!.uniqueID

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
                    if (currentPack.mayPlayCard(player, card, currentRound.bet.trumpSuit, Suit.SPADE)) {
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setMessage("Play " + card.getDisplayName() + "?")
                                .setPositiveButton(R.string.dialog_yes_button, DialogInterface.OnClickListener { dialogInterface, i ->
                                    player.hand.remove(card)
                                    multiplayerCommunicator.playCard(currentTurn, card)
                                    dialogInterface.dismiss()
                                })
                                .setNegativeButton(R.string.dialog_no_button, DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                })
                                .show()
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
                try {
                    var minimumAllowedBet =
                            round.getMinimumAllowedBetForSuit(Suit.SPADE, nextBettingPlayer)
                    btnSpades.isChecked = true
                    edtBetNrPacks.setText(minimumAllowedBet.nrPacks.toString())
                } catch (e:SuitBetNotAllowedException) {

                }
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

        setPlayerLabelValues(currentPack.turns[0].player, txtPlay1)
        setPlayerLabelValues(currentPack.turns[1].player, txtPlay2)
        setPlayerLabelValues(currentPack.turns[2].player, txtPlay3)
        setPlayerLabelValues(currentPack.turns[3].player, txtPlay4)

        if (round.bet.trumpSuit == Suit.NULLSUIT) {
            txtBet.text = "Current Bet:"
        } else {
            txtBet.text = "Current Bet:" + round.bet.nrPacks + " of " + round.bet.getTrumpTitle()
        }

        imgPlay1.setImageResource(currentPack.turns[0].playedCard.imgResId)
        imgPlay2.setImageResource(currentPack.turns[1].playedCard.imgResId)
        imgPlay3.setImageResource(currentPack.turns[2].playedCard.imgResId)
        imgPlay4.setImageResource(currentPack.turns[3].playedCard.imgResId)


    }

    private fun setPlayerLabelValues(player: Player, textView: TextView) {
        textView.text = player.name
        if (currentTurn.player.uniqueID == player.uniqueID) {
            textView.setTextColor(resources.getColor(R.color.brightGreen))
        } else {
            textView.setTextColor(resources.getColor(R.color.offWhite))
        }
    }

    private fun determinePlayer(round: Round):Player {
            for (player in round.players) {
                if (player.uniqueID == uid) {
                    return player
                }
            }
        return Player()
    }

    private fun determinePlayer(match: Match):Player {
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
            val bet = getBetFromPlayerInput();
            if (bet != null) {
                multiplayerCommunicator.placeBet(currentRound, bet)
            }
        } catch (e: SuitBetNotAllowedException) {
            val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            builder.setMessage(e.message)
                .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                .show()
        }
    }

    private fun getBetFromPlayerInput(): Bet? {
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
            Log.println(Log.WARN, "PlaceBet", "Unable to parse input for nrOfPacks [" + edtBetNrPacks.text.toString() + "] - defaulting to 6")
        }
        val possibleBet = Bet(suit, player, nrPacks)
        var promptMessage = ""
        try {
            if (currentRound.mayPlaceBet(possibleBet)) {
                return possibleBet
            } else {
                promptMessage = "You cannot place this bet, a bigger bet already exists.";
            }
        } catch (e:SuitBetNotAllowedException) {
            promptMessage = e.message.toString()
        }

        val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        builder.setMessage(promptMessage)
                .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                .show()

        return null
    }

    fun btnSuitSelectClicked(view: View) {
        estimateWinningScoreForUserInput()
    }

    private fun estimateWinningScoreForUserInput() {
        val bet = getBetFromPlayerInput()
        if (bet != null) {
            txtScorePrediction.text = (bet.trumpSuit.trumpWeight + ((bet.nrPacks - 6) * 50)).toString()
        } else {
            txtScorePrediction.text = "-"
        }
    }

    fun btnStartRoundClicked(view: View) {
        multiplayerCommunicator.startRound(currentRound)
    }

    fun btnExitClicked(view: View) {
        multiplayerCommunicator.disconnect()
        multiplayerCommunicator.endMatch(match)
        Toast.makeText(this, "Thanks for playing!", Toast.LENGTH_SHORT).show()
        finish()
    }
}