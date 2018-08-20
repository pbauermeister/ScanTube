package digital.bauermeister.scantube

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
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


const val PERMISSION_REQUEST_CODE_CAMERA = 1

class MainActivity : Activity() {
    var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()
        setContentView(R.layout.activity_main)
        // hideNavigation() // hiding the navigation would eat the next touch to reshow nav
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        camera = Camera(this, main_cameraView, PERMISSION_REQUEST_CODE_CAMERA)

        main_album_button.setOnClickListener {
            takeAndProcessPicture(true)
        }

        main_song_button.setOnClickListener {
            takeAndProcessPicture(false)
        }

        setState(State.READY)
    }

    fun hideNavigation() {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    fun hideStatusBar() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onResume() {
        super.onResume()
        camera?.start() // (re)start camera at resume, to account for device orientation changes
    }

    override fun onPause() {
        super.onPause()
        camera?.stop()
    }

    fun logger(message: String) {
        Log.i(this.javaClass.name, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        camera?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun handleBitmap(bitmapPhoto: BitmapPhoto, forAlbum: Boolean) {
        val activity = this
        doAsync {
            processBitmap(activity, bitmapPhoto, forAlbum,
                    getScreenOrientation(),
                    { logger(it) }, { state, bitmap -> setState(state, bitmap) })
        }
    }

    fun setState(state: State, bitmap: Bitmap? = null) {
        runOnUiThread {
            when (state) {
                State.SHOOTING -> {
                    // avoid reflections on CD cover: black screen, no nav
                    main_blacklayer.visibility = VISIBLE
                    main_croppedImage.visibility = GONE
                    main_buttons.visibility = INVISIBLE
                    main_cameraView.visibility = VISIBLE
                    hideNavigation()
                }
                State.READY -> {
                    runBlocking { delay(theConfig.delayBeforeReadyAgain) }

                    main_blacklayer.visibility = INVISIBLE
                    main_croppedImage.visibility = GONE
                    main_buttons.visibility = VISIBLE
                    main_cameraView.visibility = VISIBLE
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

    fun takeAndProcessPicture(forAlbum: Boolean) = async {
        setState(State.SHOOTING)

        // reset camera before shooting
        camera?.stop()
        camera?.start()

        //runBlocking { delay(theConfig.delayBeforeShooting) }

        var tmp: BitmapPhoto? = null
        async {
            tmp = suspendCoroutine {
                camera?.takePicture({ bp -> it.resume(bp) })
            }
        }.await()
        val bitmapPhoto = tmp

        if (bitmapPhoto != null)
            handleBitmap(bitmapPhoto, forAlbum)
    }

    fun getScreenOrientation(): Int {
        val screenOrientation = (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.orientation
        return when (screenOrientation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            else -> 270
        }
    }
}