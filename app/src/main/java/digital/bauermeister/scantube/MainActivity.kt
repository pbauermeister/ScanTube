package digital.bauermeister.scantube

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Toast
import io.fotoapparat.result.BitmapPhoto
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.doAsync
import kotlin.coroutines.experimental.suspendCoroutine


const val PERMISSION_REQUEST_CODE_CAMERA = 1

class MainActivity : AppCompatActivity() {
    var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    override fun onResume() {
        super.onResume()
        hideNavigation()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideNavigation()
    }

    override fun onStart() {
        super.onStart()
        camera?.start()
    }

    override fun onStop() {
        super.onStop()
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
            processBitmap(activity, bitmapPhoto, forAlbum, { logger(it) },
                    { state, bitmap -> setState(state, bitmap) })
        }
    }

    fun setState(state: State, bitmap: Bitmap? = null) {
        runOnUiThread {
            when (state) {
                State.SHOOTING -> {
                    main_blacklayer.visibility = VISIBLE
                    main_croppedImage.visibility = GONE

                    val decorView = window.decorView
                    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                    decorView.systemUiVisibility = uiOptions
                }
                State.QUERYING -> {
                    main_blacklayer.visibility = VISIBLE
                    main_croppedImage.visibility = VISIBLE
                    main_croppedImage.setImageBitmap(bitmap)
                }
                else -> {
                    main_blacklayer.visibility = INVISIBLE
                    main_croppedImage.visibility = GONE
                }
            }
        }
    }

    fun takeAndProcessPicture(forAlbum: Boolean) = async {
        setState(State.SHOOTING)
        runBlocking { delay(100) }

        var tmp: BitmapPhoto? = null
        async {
            tmp = suspendCoroutine {
                camera?.takePicture({ bp -> it.resume(bp) })
            }
        }.await()
        val bitmapPhoto = tmp

        //runBlocking { delay(100) }

        if (bitmapPhoto != null)
            handleBitmap(bitmapPhoto, forAlbum)
    }
}