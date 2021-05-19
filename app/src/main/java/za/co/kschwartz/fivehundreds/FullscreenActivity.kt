package za.co.kschwartz.fivehundreds

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_fullscreen.*
import za.co.kschwartz.fivehundreds.domain.Match
import za.co.kschwartz.fivehundreds.network.FirebaseCommunicator
import za.co.kschwartz.fivehundreds.network.MultiplayerCommunicator
import za.co.kschwartz.fivehundreds.network.ResponseReceiver

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity(), ResponseReceiver {
    private val RC_SIGN_IN: Int = 42
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var multiplayerCommunicator:MultiplayerCommunicator = FirebaseCommunicator(this)
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()

    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (user != null) {
            showStartGameScreen()
        }
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun showStartGameScreen() {
        val displayName = getDisplayName()
        llLogin.visibility = View.GONE
        llStartGame.visibility = View.VISIBLE
        llLogOut.visibility = View.VISIBLE
        Toast.makeText(applicationContext, "Welcome $displayName",Toast.LENGTH_SHORT).show()
        txtUser.text = displayName
    }

    private fun getDisplayName() = (user?.displayName ?: "??Broken User??")

    private fun showLoginScreen() {
        llLogin.visibility = View.VISIBLE
        llStartGame.visibility = View.GONE
        llLogOut.visibility = View.INVISIBLE
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

    fun howToPlayClicked(view: View) {
        val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        builder.setMessage(R.string.dialog_how_to_play)
            .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            .show()
    }

    fun loginWithGoogleClicked(view: View) {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().currentUser
                showStartGameScreen()
            } else {
                val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                builder.setMessage(R.string.dialog_login_failed)
                    .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                    })
                    .show()
            }
        }
    }

    fun logOut(view: View) {
        val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        builder.setMessage(R.string.dialog_logout_prompt)
            .setPositiveButton(R.string.dialog_yes_button, DialogInterface.OnClickListener { dialogInterface, i ->
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        showLoginScreen()
                    }
            })
            .setNegativeButton(R.string.dialog_no_button, DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            .show()
    }

    fun btnStartNewGameClicked(view: View) {
        val match = multiplayerCommunicator.createMatch()
    }

    override fun joinMatchSuccess(match: Match) {
        Toast.makeText(applicationContext, "Joining match "+match.uniqueMatchCode+"...",Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LobbyActivity::class.java).apply {
            putExtra("GAMEID", match.uniqueMatchCode)
        }
        startActivity(intent)
    }

    override fun joinMatchFailure(message: String) {
        val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        builder.setMessage(message)
            .setPositiveButton(R.string.dialog_ok_button, DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            .show()
    }

    override fun leaveMatchSuccess(match: Match) {
        //Nothing
    }

    override fun matchUpdated(match: Match) {
        //Nothing
    }

    fun btnJoinGameClicked(view: View) {
        val gameID = inputGameID.text.toString()
        if (gameID.length != 6){
            Toast.makeText(applicationContext, "Please enter a valid Game ID",Toast.LENGTH_SHORT).show()
        } else {
            multiplayerCommunicator.joinMatch(gameID, getDisplayName())
        }
    }
}
