package digital.bauermeister.scantube

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceActivity
import android.text.Html
import android.text.SpannableString
import android.text.util.Linkify
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.widget.Toast
import io.fotoapparat.result.BitmapPhoto
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.doAsync
import kotlin.coroutines.experimental.suspendCoroutine
import android.text.method.LinkMovementMethod
import android.widget.TextView




const val PERMISSION_REQUEST_CODE_CAMERA = 1

class MainActivity : Activity() {
    var camera: Camera? = null
    var buttons = getPrefButtons()
    var youTubeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()
        setContentView(R.layout.activity_main)
        // hideNavigation() // hiding the navigation would eat the next touch to reshow nav
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!theConfig.showAlbumButton) main_album_button.visibility = GONE
        if (!theConfig.showSongButton) main_song_button.visibility = GONE
        if (!theConfig.showReplayButton) main_replay_button.visibility = GONE

        camera = Camera(this, main_cameraView, PERMISSION_REQUEST_CODE_CAMERA)

        main_settings.setOnClickListener {
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
        }

        main_album_button.setOnClickListener {
            if (checkGoogleKeys())
                takeAndProcessPicture(true)
        }

        main_song_button.setOnClickListener {
            if (checkGoogleKeys())
                takeAndProcessPicture(false)
        }

        main_replay_button.setOnClickListener {
            if (checkGoogleKeys())
                launchYouTube(this, youTubeUrl)
        }

        setState(State.READY)

        checkGoogleKeys()
    }

    private fun checkGoogleKeys(): Boolean {
        val ok = theConfig.googleVisionAppKey != null && theConfig.youTubeAppKey != null
        if (!ok) {
            showKeysMissingDialog()
        }
        return ok
    }

    private fun hideNavigation() {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    private fun hideStatusBar() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onResume() {
        super.onResume()

        val buttonsNow = getPrefButtons()
        if (buttons != buttonsNow) {
            // pref changed
            buttons = buttonsNow
            this.recreate()
        } else {
            camera?.stop()
            camera?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        camera?.stop()
    }

    fun message(message: String?, steady: Boolean) {
        if (message != null)
            Log.i(this.javaClass.name, message)
        runOnUiThread {
            if (steady) {
                if (message == null) {
                    main_message.text = null
                    main_message.visibility = View.GONE
                } else {
                    main_message.text = message
                    main_message.visibility = View.VISIBLE
                }
            } else if (message != null) {
                main_message.visibility = View.GONE
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearMessage() {
        main_message.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        camera?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleBitmap(bitmapPhoto: BitmapPhoto, forAlbum: Boolean) {
        val activity = this
        message(activity.getString(R.string.message_image_captured), false)
        doAsync {
            processBitmap(activity, bitmapPhoto, forAlbum,
                    getScreenOrientation(),
                    { str, long -> message(str, long) },
                    { state, bitmap -> setState(state, bitmap) })
        }
    }

    private fun setState(state: State, bitmap: Bitmap? = null) {
        runOnUiThread {
            when (state) {
                State.SHOOTING -> {
                    // avoid reflections on CD cover: black screen, no nav
                    main_blacklayer.visibility = VISIBLE
                    main_croppedImage.visibility = GONE
                    main_buttons.visibility = INVISIBLE
                    main_cameraView.visibility = VISIBLE
                    main_youtube.visibility = INVISIBLE
                    hideNavigation()
                }
                State.EVALUATING -> {
                    // avoid reflections on CD cover: black screen, no nav
                    main_blacklayer.visibility = INVISIBLE
                    main_croppedImage.visibility = GONE
                    main_buttons.visibility = INVISIBLE
                    main_cameraView.visibility = INVISIBLE
                    main_youtube.visibility = VISIBLE
                    hideNavigation()
                }
                State.READY -> {
                    runBlocking { delay(theConfig.delayBeforeReadyAgain) }
                    clearMessage()
                    main_replay_button.isEnabled = youTubeUrl != null

                    main_blacklayer.visibility = INVISIBLE
                    main_croppedImage.visibility = GONE
                    main_buttons.visibility = VISIBLE
                    main_cameraView.visibility = VISIBLE
                    main_youtube.visibility = INVISIBLE
                    camera?.start()
                }
                State.ERROR -> {
                    main_blacklayer.visibility = INVISIBLE
                    main_croppedImage.visibility = GONE
                    main_buttons.visibility = VISIBLE
                    main_cameraView.visibility = VISIBLE
                    main_youtube.visibility = INVISIBLE
                    camera?.start()
                }
                else -> {
                    camera?.stop()
                    main_blacklayer.visibility = GONE
                    main_croppedImage.visibility = VISIBLE
                    main_buttons.visibility = INVISIBLE
                    main_cameraView.visibility = INVISIBLE
                    main_croppedImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun takeAndProcessPicture(forAlbum: Boolean) = async {
        setState(State.SHOOTING)

        if (theConfig.resetCameraBeforeShooting) {
            camera?.stop()
            camera?.start()
        }

        if (theConfig.delayBeforeShooting > 0)
            runBlocking { delay(theConfig.delayBeforeShooting) }

        var capture: BitmapPhoto? = null
        async {
            capture = suspendCoroutine {
                message(getString(R.string.message_taking_picture), false)
                camera?.takePicture({ bp -> it.resume(bp) })
            }
        }.await()
        val bitmapPhoto = capture

        if (bitmapPhoto != null)
            handleBitmap(bitmapPhoto, forAlbum)
    }

    private fun getScreenOrientation(): Int {
        val screenOrientation = (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.orientation
        return when (screenOrientation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            else -> 270
        }
    }

    fun onYouTubeLaunched(youTubeUrl: String, label: String) {
        this.youTubeUrl = youTubeUrl
        val mainText = getText(R.string.button_replay)
        val smallText = Html.escapeHtml(label)
        runOnUiThread {
            main_replay_button.setText(Html.fromHtml(
                    "$mainText<br/><small>$smallText</small>"))
        }

    }

    private fun showKeysMissingDialog() {
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle(R.string.dialog_keys_title)

        // Display a message on alert dialog
        val msg = getText(R.string.dialog_keys_message)
        val ss = SpannableString(msg);
        Linkify.addLinks(ss, Linkify.ALL);
        builder.setMessage(ss)

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(R.string.dialog_button_positive_button) { dialog, which ->
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.AppKeysPreferenceFragment::class.java.name);
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            startActivity(intent)
        }

        // Display a negative button on alert dialog
        builder.setNegativeButton(android.R.string.cancel) { dialog, which -> }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()

        // Make the textview clickable. Must be called after show()
        (dialog.findViewById(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
    }
}