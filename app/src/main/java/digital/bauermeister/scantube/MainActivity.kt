package digital.bauermeister.scantube

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync


const val PERMISSION_REQUEST_CODE_CAMERA = 1

class MainActivity : AppCompatActivity() {
    var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        camera = Camera(this, main_cameraView, PERMISSION_REQUEST_CODE_CAMERA)

        main_button.setOnClickListener {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()
            camera?.takePicture({ bitmap -> handleBitmap(bitmap) })
        }
    }

    override fun onStart() {
        super.onStart()
        camera?.start()
    }

    override fun onStop() {
        super.onStop()
        camera?.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        camera?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun handleBitmap(bitmap: Bitmap) {
        val activity = this
        doAsync {
            processBitmap(activity, bitmap)
        }
    }
}
